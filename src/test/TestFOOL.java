package test;

import compiler.exc.*;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static test.TestUtils.*;

public class TestFOOL {

	private TestUtils compiler;

	@BeforeEach
	public void setup() {
		compiler = new TestUtils();
	}

	@Test
	public void simple() throws TypeException {
		int result = Integer.parseInt(compileAndRun("print(1+2*3);").get(0));
		assertEquals(result, 7);
	}

	@Test
	public void booleans_as_ints() throws TypeException {
		assertEquals(compileAndRun("print(true);").get(0), "1");
		assertEquals(compileAndRun("print(false);").get(0), "0");
	}

	@Test
	public void simple_if() throws TypeException {
		assertEquals(compileAndRun("print(if 2+2 == 3 then {1} else {5});").get(0), "5");
	}

	@Test
	public void simple_var() throws TypeException {
		assertEquals(compileAndRun("let var x:int = 5; in print(x);").get(0), "5");
		assertEquals(compileAndRun("let var x:bool = true; in print(x);").get(0), "1");
	}

	@Test
	public void simple_fun() throws TypeException {
		String code = """
					let
						fun f:int()
						let
							var x:int = 5;
						in
							x;
					in
						print(f());
					""";
		assertEquals(compileAndRun(code).get(0), "5");
	}

	@Test
	public void simple_math() throws TypeException {
		assertEquals(compileAndRun("print(1+2);").get(0), "3");
		assertEquals(compileAndRun("print(2-1);").get(0), "1");
		assertEquals(compileAndRun("print(2*3);").get(0), "6");
		assertEquals(compileAndRun("print(10/5);").get(0), "2");
	}

	@Test
	public void division_by_zero() {
		assertThrows(ArithmeticException.class, () -> compileAndRun("1/0;"));
	}

	@Test
	public void negative_numbers() throws TypeException {
		assertEquals(compileAndRun("print(1-2);").get(0), "-1");
		assertEquals(compileAndRun("print(-2-1);").get(0), "-3");
		assertEquals(compileAndRun("print(1+-2);").get(0), "-1");
		assertEquals(compileAndRun("print(2*-3);").get(0), "-6");
		assertEquals(compileAndRun("print(-2*3);").get(0), "-6");
		assertEquals(compileAndRun("print(-2*-3);").get(0), "6");
		assertEquals(compileAndRun("print(6/-3);").get(0), "-2");
		assertEquals(compileAndRun("print(-6/3);").get(0), "-2");
		assertEquals(compileAndRun("print(-6/-3);").get(0), "2");
	}

	@Test
	public void test_eq() throws TypeException {
		assertEquals(compileAndRun("print(1==2);").get(0), "0");
		assertEquals(compileAndRun("print(2==2);").get(0), "1");
		assertEquals(compileAndRun("print(3==2);").get(0), "0");
	}

	@Test
	public void test_geq() throws TypeException {
		assertEquals(compileAndRun("print(1>=2);").get(0), "0");
		assertEquals(compileAndRun("print(2>=2);").get(0), "1");
		assertEquals(compileAndRun("print(3>=2);").get(0), "1");
	}

	@Test
	public void test_leq() throws TypeException {
		assertEquals(compileAndRun("print(1<=2);").get(0), "1");
		assertEquals(compileAndRun("print(2<=2);").get(0), "1");
		assertEquals(compileAndRun("print(3<=2);").get(0), "0");
	}

	@Test
	public void test_or() throws TypeException {
		assertEquals(compileAndRun("print(false||false);").get(0), "0");
		assertEquals(compileAndRun("print(false||true);").get(0), "1");
		assertEquals(compileAndRun("print(true||false);").get(0), "1");
		assertEquals(compileAndRun("print(true||true);").get(0), "1");
	}

	@Test
	public void only_bool_in_or() {
		assertThrows(TypeException.class, () -> compileAndRun("print(3 || 2);"));
		assertThrows(TypeException.class, () -> compileAndRun("print(3 || true);"));
		assertThrows(TypeException.class, () -> compileAndRun("print(true || 2);"));
	}

	@Test
	public void test_and() throws TypeException {
		assertEquals(compileAndRun("print(false&&false);").get(0), "0");
		assertEquals(compileAndRun("print(false&&true);").get(0), "0");
		assertEquals(compileAndRun("print(true&&false);").get(0), "0");
		assertEquals(compileAndRun("print(true&&true);").get(0), "1");
	}

	@Test
	public void only_bool_in_and() {
		assertThrows(TypeException.class, () -> compileAndRun("print(3 && 2);"));
		assertThrows(TypeException.class, () -> compileAndRun("print(3 && true);"));
		assertThrows(TypeException.class, () -> compileAndRun("print(true && 2);"));
	}

	@Test
	public void test_not() throws TypeException {
		assertEquals(compileAndRun("print(!false);").get(0), "1");
		assertEquals(compileAndRun("print(!true);").get(0), "0");
	}

	@Test
	public void only_bool_in_not() {
		assertThrows(TypeException.class, () -> compileAndRun("print(!0);"));
		assertThrows(TypeException.class, () -> compileAndRun("print(!1);"));
		assertThrows(TypeException.class, () -> compileAndRun("print(!2);"));
	}

	@Test
	public void efficient_or() throws TypeException {
		// This code tests if || is efficiently evaluated
		// We prepare an infinite recursive function f() that will loop the program if executed
		// Then we check (true || f()) and the program should terminate
		String code = """
					let
						fun f:bool() (
							f()
						);
					in
						print(
							if (true || f()) then {
								1
							} else {
								0
							}
						);""";
		assertEquals(compileAndRun(code).get(0), "1");
	}

	@Test
	public void efficient_and() throws TypeException {
		// This code tests if && is efficiently evaluated
		// We prepare an infinite recursive function f() that will loop the program if executed
		// Then we check (false && f()) and the program should terminate
		String code = """
					let
						fun f:bool() (
							f()
						);
					in
						print(
							if (false && f()) then {
								1
							} else {
								0
							}
						);""";
		assertEquals(compileAndRun(code).get(0), "0");
	}

	@Test
	public void wrong_return_type_function() throws TypeException {
		String code = """
					let
						fun f:bool() let var x:int=5; in x;
					in
						f();""";
		compiler.compileSource(code);
		assertFalse(compiler.err.ok());
	}

	@Test
	public void incompatible_value_var() throws TypeException {
		String code = """
					let
						var x:bool = 5;
					in
						x;""";
		compiler.compileSource(code);
		assertFalse(compiler.err.ok());
	}

	@Test
	public void non_bool_in_if() {
		String c1 = """
					if(5) then {true} else {false};""";
		String c2 = """
					let
						fun f:int() (5);
					in
						if (f) then {true} else {false};
				""";
		assertThrows(TypeException.class, () -> compile(c1));
		assertThrows(TypeException.class, () -> compile(c2));
	}

	@Test
	public void different_types_in_if() {
		String c1 = """
					let
						fun f:int() (5);
					in
						if (true) then {true} else {f};
				""";
		String c2 = """
					let
						fun f:int() (5);
					in
						if (true) then {f} else {true};
				""";
		assertThrows(TypeException.class, () -> compile(c1));
		assertThrows(TypeException.class, () -> compile(c2));
	}

	@Test
	public void incompatible_types_in_equal() {
		String code = """
					let
						fun f:int() (5);
					in
						if (true==f) then {1} else {0};
				""";
		assertThrows(TypeException.class, () -> compile(code));
	}

	@Test
	public void incompatible_types_in_greaterequal() {
		String code = """
					let
						fun f:int() (5);
					in
						if (true>=f) then {1} else {0};
				""";
		assertThrows(TypeException.class, () -> compile(code));
	}

	@Test
	public void incompatible_types_in_lessequal() {
		String code = """
					let
						fun f:int() (5);
					in
						if (true==f) then {1} else {0};
				""";
		assertThrows(TypeException.class, () -> compile(code));
	}

	@Test
	public void only_int_in_mul() {
		String code = """
					let
						fun f:int() (5);
					in
						5*f;
				""";
		assertThrows(TypeException.class, () -> compile(code));
	}

	@Test
	public void only_int_in_div() {
		String code = """
					let
						fun f:int() (5);
					in
						f/5;
				""";
		assertThrows(TypeException.class, () -> compile(code));
	}

	@Test
	public void only_int_in_sum() {
		String code = """
					let
						fun f:int() (5);
					in
						5+f;
				""";
		assertThrows(TypeException.class, () -> compile(code));
	}

	@Test
	public void only_int_in_sub() {
		String code = """
					let
						fun f:int() (5);
					in
						5-f;
				""";
		assertThrows(TypeException.class, () -> compile(code));
	}

	@Test
	public void non_function_invocation() {
		String code = """
					let
						var x:int = 5;
					in
						x();
				""";
		assertThrows(TypeException.class, () -> compile(code));
	}

	@Test
	public void wrong_number_of_parameters() {
		String c1 = """
					let
						fun f:int() (5);
					in
						f(5);
				""";
		String c2 = """
					let
						fun f:int(x:int) (5);
					in
						f();
				""";
		assertThrows(TypeException.class, () -> compile(c1));
		assertThrows(TypeException.class, () -> compile(c2));
	}

	@Test
	public void wrong_parameter_type() {
		String code = """
					let
						fun f:int(x:bool) (5);
					in
						f(5);
				""";
		assertThrows(TypeException.class, () -> compile(code));
	}

	@Test
	public void unexisting_variable() {
		String code = """
					x;
				""";
		assertThrows(IncomplException.class, () -> compile(code));
	}

	@Test
	public void variable_redefinition() throws TypeException {
		String code = """
					let
						var x:bool = true;
						var x:int = 5;
					in
						x;
				""";
		compiler.compileSource(code);
		assertFalse(compiler.err.ok());
	}

	@Test
	public void function_redefinition() throws TypeException {
		String code = """
					let
						fun f:bool() (true);
						fun f:int() (5);
					in
						f();
				""";
		compiler.compileSource(code);
		assertFalse(compiler.err.ok());
	}

	@Test
	public void function_argument_redefinition() {
		String code = """
					let
						fun f:bool(x:int, x:bool) (true);
					in
						f();
				""";
		assertThrows(TypeException.class, () -> compile(code));
	}

	@Test
	public void fibonacci() throws TypeException {
		String code = """
					let
						fun fib:int (n:int) (
							if (n <= 1) then {
								n
							} else {
								fib(n-1) + fib(n-2)
							}
						);
					in
						print(fib(10));
				""";
		assertEquals(compileAndRun(code).get(0), "55");
	}

	@Test
	public void fattoriale() throws TypeException {
		String code = """
					let
						fun fatt:int (n:int) (
							if (n <= 1) then {
								1
							} else {
								n * fatt(n-1)
							}
						);
					in
						print(fatt(10));
				""";
		assertEquals(compileAndRun(code).get(0), "3628800");
	}

	@Test
	public void euclid_gcd() throws TypeException {
		String code = """
					let
						fun rem:int (a:int, b:int) (
							if (a >= b) then {
								rem(a-b, b)
							} else {
								a
							}
						);
						fun gcd:int (a:int, b:int) (
							if (b == 0) then {
								a
							} else {
								gcd(b, rem(a, b))
							}
						);
					in
						print(gcd(12345, 60));
				""";
		assertEquals(compileAndRun(code).get(0), "15");
	}

	@Test
	public void primes() throws TypeException {
		String code = """
					let
						fun rem:int (a:int, b:int) (
							if (a >= b) then {
								rem(a-b, b)
							} else {
								a
							}
						);
						fun isPrime:bool (i:int, n:int) (
							if (i == n) then {
								true
							} else {
								if (rem(n, i) == 0) then {
									false
								} else {
									isPrime(i+1, n)
								}
							}
						);
						fun prime:bool (n:int) (
							if (n <= 1) then {false}
							else {
								if (n == 2) then {true}
								else {
									isPrime(2, n)
								}
							}
						);
						fun primes:bool (n:int) (
							if (n <= 100) then {
								if (prime(n)) then {
									if (print(n) >= 0) then {      /* print(n) returns n */
										primes(n+1)
									} else {
										primes(n+1)
									}
								} else { primes(n+1) }
							} else {true}
						);
					in
						primes(0);
				""";
		List<String> p = Stream.of(2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61, 67, 71, 73, 79, 83, 89, 97).map(Object::toString).toList();
		assertEquals(p, compileAndRun(code));
	}

	@Test
	public void square_root() throws TypeException {
		String code = """
					let
						fun subOddNumber:int (i:int, n:int, count:int) (
							if (i >= n+1) then {
								count
							} else {
								subOddNumber(i+2, n-i, count+1)
							}
						);
						fun sqrt:int (n:int) (
							subOddNumber(1, n, 0)
						);
					in
						print(sqrt(59049));
				""";
		assertEquals(compileAndRun(code).get(0), "243");
	}

	@Test
	public void square_root_binary_search() throws TypeException {
		String code = """
					let
						fun sqrtBinarySearch:int (low:int, high:int, mid:int, n:int)
						let
							var tmp:int = mid*mid;
						in
							if (tmp == n) then {
								mid
							} else {
								if (high-low <= 1) then {
									low
								} else {
									if (tmp >= n) then {
										sqrtBinarySearch(low, mid, (low+mid)/2, n)
									} else {
										sqrtBinarySearch(mid, high, (mid+high)/2, n)
									}
								}
							}
						;
						fun sqrt:int (n:int) (
							sqrtBinarySearch(0, n, n/2, n)
						);
					in
						print(sqrt(59049));
				""";
		assertEquals(compileAndRun(code).get(0), "243");
	}

	@Test
	public void collatz_conjecture() throws TypeException {
		String code = """
					let
						fun rem:int (a:int, b:int) (
							if (a >= b) then {
								rem(a-b, b)
							} else {
								a
							}
						);
						fun collatz:int (n:int) (
							if (n == 1) then {
								1
							} else {
								if (rem(n, 2) == 0) then {
									collatz(n/2)
								} else {
									collatz(3*n+1)
								}
							}
						);
					in
						print(collatz(245));
				""";
		assertEquals(compileAndRun(code).get(0), "1");
	}

	@Test
	public void prova() throws TypeException {
		String code = """
					let
						var y:int = 5+2;
						fun g:bool (b:bool)
							let
								fun f:bool (n:int, m:int)
									let
										var x:int = m;
									in g(3==y);
							in
								if b then {
									f(2,3)
								} else {
									false
								};
					in
						print (
							if g(true)
								then { y }
								else { 10 }
						);
				""";
		assertEquals(compileAndRun(code).get(0), "10");
	}

	@Test
	public void prova2() throws TypeException {
		String code = """
					let
						var x:int = 1+5;
						var b:bool = true;
					in
						print (
							if (b) then {
									x+1
							} else {
									x+2
							}
						);
				""";
		assertEquals(compileAndRun(code).get(0), "7");
	}

	@Test
	public void prova3() throws TypeException {
		String code = """
					let
						fun f:int (i:int, j:int) i+j;
					in
						print (f(3,5));
				""";
		assertEquals(compileAndRun(code).get(0), "8");
	}

	@Test
	public void esempio() throws TypeException {
		String code = """
					let
						var x:int = 5+3;
						fun f:bool (n:int, m:int)
						let
							var x:bool = true;
						in x==(n==m);
					in
						print (
							if f(x,8) then {
								false
							} else { 10 }
						);
				""";
		assertEquals(compileAndRun(code).get(0), "0");
	}

	// Object-Oriented tests

	@Test
	public void empty_class() throws TypeException {
		String code = """
					let
						class example() {}
					in
						5;
				""";
		compiler.compileSource(code);
		assertTrue(compiler.err.ok());
	}

	@Test
	public void class_with_fields() throws TypeException {
		String code = """
					let
						class example(x:int) {}
					in
						5;
				""";
		compiler.compileSource(code);
		assertTrue(compiler.err.ok());
	}

	@Test
	public void class_with_methods() throws TypeException {
		String code = """
					let
						class example() {
							fun f:bool() (true);
						}
					in
						5;
				""";
		compiler.compileSource(code);
		assertTrue(compiler.err.ok());
	}

	@Test
	public void class_with_fields_and_methods() throws TypeException {
		String code = """
					let
						class example(x:int) {
							fun f:bool() (true);
						}
					in
						5;
				""";
		compiler.compileSource(code);
		assertTrue(compiler.err.ok());
	}

	@Test
	public void null_type() throws TypeException {
		String code = """
					let
						class useless() {}
					in
						print(null);
				""";
		assertEquals(compiler.compileSourceAndRun(code).get(0), "-1");
		assertTrue(compiler.err.ok());
	}

	@Test
	public void null_is_not_int() {
		// we are checking null against -1 because null is represented as -1 in memory
		String code = """
					if (null == -1) then {
						true
					} else {
						false
					};
				""";
		assertThrows(TypeException.class, () -> compile(code));
	}

	@Test
	public void classes_only_in_global_scope() throws TypeException {
		String code = """
					let
						fun f:int ()
						let
							class example() {}
						in 1;
					in 1;
				""";
		compiler.quiet().compileSource(code);
		assertFalse(compiler.err.ok());
	}

	@Test
	public void class_redefinition() throws TypeException {
		String code = """
					let
						class example() {}
						class example() {}
					in 1;
				""";
		compiler.quiet().compileSource(code);
		assertFalse(compiler.err.ok());
	}

	@Test
	public void object_redefinition() throws TypeException {
		String code = """
					let
						class example() {}
						var x:example = new example();
						var x:example = new example();
					in 1;
				""";
		compiler.quiet().compileSource(code);
		assertFalse(compiler.err.ok());
	}

	@Test
	public void field_redefinition() throws TypeException {
		String code = """
					let
						class example(x:int, x:bool) {}
					in 1;
				""";
		compiler.quiet().compileSource(code);
		assertFalse(compiler.err.ok());
	}

	@Test
	public void method_redefinition() throws TypeException {
		String code = """
					let
						class example() {
							fun f:bool() (true);
							fun f:int() (5);
						}
					in 1;
				""";
		compiler.quiet().compileSource(code);
		assertFalse(compiler.err.ok());
	}

	@Test
	public void method_argument_redefinition() throws TypeException {
		String code = """
					let
						class example() {
							fun f:bool(x:int, x:bool) (true);
						}
					in 1;
				""";
		compiler.quiet().compileSource(code);
		assertFalse(compiler.err.ok());
	}

	@Test
	public void cannot_access_fields() throws TypeException {
		String code = """
					let
						class example(a:int) {}
						var x:example = new example(5);
					in x.a;
				""";
		compiler.quiet().compileSource(code);
		assertFalse(compiler.err.ok());
	}

	@Test
	public void using_method_like_field() throws TypeException {
		String code = """
					let
						class example() {
							fun f:bool() (true);
						}
						var x:example = new example();
					in x.f;
				""";
		compiler.quiet().compileSource(code);
		assertFalse(compiler.err.ok());
	}

	@Test
	public void unexisting_class() throws TypeException {
		String code = """
					let
						var x:example = new example();
					in 1;
				""";
		compiler.quiet().compileSource(code);
		assertFalse(compiler.err.ok());
	}

	@Test
	public void calling_method_on_unexisting_object() throws TypeException {
		String code = """
					let
						class example() {
							fun m:bool() (true);
						}
					in
						obj.m();
				""";
		try {
			compiler.quiet().compileSource(code);
			assertFalse(compiler.err.ok());
		} catch (NullPointerException e) {/* ignore */}
	}

	@Test
	public void calling_unexisting_method() throws TypeException {
		String code = """
					let
						class example() {
							fun m:bool() (true);
						}
						var obj:example = new example();
					in
						obj.f();
				""";
		try {
			compiler.quiet().compileSource(code);
			assertFalse(compiler.err.ok());
		} catch (NullPointerException e) {/* ignore */}
	}

	@Test
	public void calling_method_on_wrong_class() throws TypeException {
		String code = """
					let
						class example() {
							fun m:bool() (true);
						}
						class simple() {
							fun x:bool() (true);
						}
						var obj:example = new example();
					in
						obj.x();
				""";
		try {
			compiler.quiet().compileSource(code);
			assertFalse(compiler.err.ok());
		} catch (NullPointerException e) {/* ignore */}
	}

	@Test
	public void function_returning_object() throws TypeException {
		String code = """
					let
						class example(x:int) {
							fun m:bool() (true);
						}
						fun f:example() (new example(5));
					in
						f();
				""";
		compiler.quiet().compileSource(code);
		assertTrue(compiler.err.ok());
	}

	@Test
	public void method_returning_object() throws TypeException {
		String code = """
					let
						class example(a:int) {
							fun m:example() (new example(5));
						}
						var x:example = new example(10);
					in
						x.m();
				""";
		compiler.quiet().compileSource(code);
		assertTrue(compiler.err.ok());
	}

	@Test
	public void null_subtype_of_class() throws TypeException {
		String code = """
					let
						class example(a:int) {
							fun m:example() (new example(5));
						}
						var x:example = new example(10);
					in
						print(x == null);
				""";
		assertEquals(compiler.compileSourceAndRun(code).get(0), "0");
		assertTrue(compiler.err.ok());
	}

	@Test
	public void less_fields_than_required() throws TypeException {
		String code = """
					let
						class example(a:int) {}
						var obj:example = new example();
					in 1;
				""";
		compiler.compileSource(code);
		assertFalse(compiler.err.ok());
	}

	@Test
	public void more_fields_than_required() throws TypeException {
		String code = """
					let
						class example(a:int) {}
						var obj:example = new example(5, 6);
					in 1;
				""";
		compiler.compileSource(code);
		assertFalse(compiler.err.ok());
	}

	@Test
	public void wrong_field_type() throws TypeException {
		String code = """
					let
						class example(a:bool) {}
						var obj:example = new example(5);
					in 1;
				""";
		compiler.compileSource(code);
		assertFalse(compiler.err.ok());
	}

	@Test
	public void method_call() throws TypeException {
		String code = """
					let
						class example(x:int) {
							fun getX:int () (x);
						}
						var obj:example = new example(5);
					in
						print(obj.getX());
				""";
		String result = compiler.compileSourceAndRun(code).get(0);
		assertTrue(compiler.err.ok());
		assertEquals(result, "5");
	}

	@Test
	public void method_call_with_two_classes() throws TypeException {
		String code = """
					let
						class useless(a:bool, b:bool) {
							fun wow:int () (a);
						}
						class example(x:int) {
							fun getX:int () (x);
						}
						var obj:example = new example(5);
					in
						print(obj.getX());
				""";
		String result = compiler.compileSourceAndRun(code).get(0);
		assertTrue(compiler.err.ok());
		assertEquals(result, "5");
	}

	@Test
	public void getter_with_three_fields() throws TypeException {
		String code = """
					let
						class example(a:int, b:int, c:int) {
							fun getA:int () (a);
							fun getB:int () (b);
							fun getC:int () (c);
						}
						var obj:example = new example(5,6,7);
					in
						print(obj.getB());
				""";
		String result = compiler.compileSourceAndRun(code).get(0);
		assertTrue(compiler.err.ok());
		assertEquals(result, "6");
	}

	@Test
	public void multiple_objects() throws TypeException {
		String code = """
					let
						class example(x:int) {
							fun getX:int () (x);
						}
						var one:example = new example(1);
						var two:example = new example(2);
						var three:example = new example(3);
					in
						print(two.getX());
				""";
		String result = compiler.compileSourceAndRun(code).get(0);
		assertTrue(compiler.err.ok());
		assertEquals(result, "2");
	}

	@Test
	public void multiple_classes_multiple_objects() throws TypeException {
		String code = """
					let
						class example(x:int) {
							fun getX:int () (x);
						}
						class useless(a:bool, b:bool) {
							fun getA:bool () (a);
							fun getB:bool () (b);
						}
						var one:example = new example(1);
						var hello:example = new useless(false, false);
						var two:example = new example(2);
						var wow:example = new useless(false, true);
						var three:example = new example(3);
						var ciao:example = new useless(true, false);
					in
						print(two.getX());
				""";
		String result = compiler.compileSourceAndRun(code).get(0);
		assertTrue(compiler.err.ok());
		assertEquals(result, "2");
	}

	@Test
	public void objects_as_function_parameters() throws TypeException {
		String code = """
					let
						class point(x:int, y:int) {
							fun getX:int () (x);
							fun getY:int () (y);
						}
						fun sum:point (a:point, b:point) (
							new point( a.getX()+b.getX(), a.getY()+b.getY() )
						);
						var first:point = new point(2, 3);
						var second:point = new point(-5, 6);
						var result:point = sum(first, second);
					in
						print(result.getX());
				""";
		String result = compiler.compileSourceAndRun(code).get(0);
		assertTrue(compiler.err.ok());
		assertEquals(result, "-3");
	}

	@Test
	public void recursive_classes() throws TypeException {
		String code = """
					let
						class list(head:int, tail:list) {
							fun getHead:int() (head);
							fun getTail:list() (tail);
							fun search:int (n:int) (
								if (head == n) then {
									head
								} else {
									if (tail == null) then {
										-1
									} else {
										search(tail)
									}
								}
							);
						}
						var l:list = new list(1, new list(2, new list(3, new list(4, null))));
					in
						print(l.search(3));
				""";
		String result = compiler.compileSourceAndRun(code).get(0);
		assertTrue(compiler.err.ok());
		assertEquals(result, "3");
	}

	@Test
	public void quicksort() throws TypeException {
		String code = """
					let
				 	class List (f:int, r:List) {
				 		fun first:int() f;
				 		fun rest:List() r;
				 	}
				 	fun printList:List (l:List)
				 	let
				 		fun makeList:List (l:List, i:int) new List (i,l);
				 	in
				 		if (l == null)
				 			then {null}
				 			else {makeList(printList(l.rest()),print(l.first()))};
				 	
				 	fun append:List (l1:List, l2:List)
				 		if (l1 == null)
				 			then {l2}
				 			else {new List(l1.first(), append(l1.rest(),l2))} ;
				 		
				 	/* filtra la lista "l" mantenendo solo gli elementi */
				 	/* che sono: <= a "pivot", se "before" è true       */
				 	/*            > a "pivot", se "before" è false      */
				 	fun filter:List (l:List, pivot:int, before:bool)\s
				 	let
				 		fun accept:bool (cond:bool)
				 				if (before) then {cond} else {!(cond)};
				 	in
				 		if (l == null) then {null}
				 		else {
				 			if (accept(l.first()<=pivot)) then {
				 				new List( l.first(), filter(l.rest(),pivot,before) )
				 			} else { filter(l.rest(),pivot,before) }
				 		};
				 	
				 	fun quicksort:List (l:List)
				 	let
				 		var pivot:int = if (l==null) then {0} else {l.first()};
				 	in
				 		if (l == null)
				 			 then {null}
				 			 else {append(
				 							 quicksort( filter(l.rest(),pivot,true) ),
				 							 new List(pivot, quicksort( filter(l.rest(),pivot,false) )	)
				 						 )};
				 																												
				 	var l:List = new List (2,
				 							new List(1,
				 										new List(4,
				 													new List (3,
				 																new List(2,
				 																			new List(5,null))))));
				 	
				 in printList(quicksort(l));
				""";
		var result = compiler.compileSourceAndRun(code);
		assertTrue(compiler.err.ok());
		assertEquals(result, Stream.of(1,2,2,3,4,5).map(Object::toString).toList());
	}

	// Object Inheritance tests

	@Test
	public void subtyping_class() throws TypeException {
		String code = """
					let
						class father() {}
						class example extends father() {}
						fun f:bool(obj:example) (true);
						var x:father = new father();
					in f(x);
				""";
		compiler.compileSource(code);
		assertTrue(compiler.err.ok());
	}

	@Test
	public void unexisting_superclass() throws TypeException {
		String code = """
					let
						class example extends father() {}
					in 1;
				""";
		compiler.compileSource(code);
		assertFalse(compiler.err.ok());
	}

	@Test
	public void accessing_father_fields() throws TypeException {
		String code = """
					let
						class father(a:int) {}
						class example extends father(b:bool) {
							fun m:int() (a);
						}
						var x:example = new example(5, true);
					in print(x.m());
				""";
		compiler.compileSource(code);
		assertTrue(compiler.err.ok());
	}

	@Test
	public void accessing_father_methods() throws TypeException {
		String code = """
					let
						class father() {
							fun m:int() (5);
						}
						class example extends father() {}
						var x:example = new example();
					in print(x.m());
				""";
		compiler.compileSource(code);
		assertTrue(compiler.err.ok());
	}

	@Test
	public void field_overriding() throws TypeException {
		String code = """
					let
						class father(a:bool) {
							fun getA:bool() (a);
						}
						class example extends father(b:bool, a:int) {
							fun getB:bool() (b);
							fun getChildA:int() (a);
						}
						var x:example = new example(true, 5);
					in print(x.getChildA());
				""";
		compiler.compileSource(code);
		assertTrue(compiler.err.ok());
	}

	@Test
	public void method_overriding() throws TypeException {
		String code = """
					let
						class father() {
							fun m:bool() (false);
						}
						class example extends father() {
							fun m:int() (5);
						}
						var x:example = new example();
					in print(x.m());
				""";
		compiler.compileSource(code);
		assertTrue(compiler.err.ok());
	}

	@Test
	public void wrong_overriding() throws TypeException {
		String code = """
					let
						class father() {
							fun m:bool() (false);
						}
						class example extends father(m:int) {}
					in 1;
				""";
		compiler.compileSource(code);
		assertFalse(compiler.err.ok());
	}

	@Test
	public void method_call_on_wrong_class() throws TypeException {
		String code = """
					let
						class father() {
							fun m:bool() (false);
						}
						class example extends father() {}
						class useless() {}
						var x:useless = new useless();
					in print(x.m());
				""";
		compiler.compileSource(code);
		assertFalse(compiler.err.ok());
	}

	// Code Optimization tests
}
