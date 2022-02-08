package test;

import compiler.exc.*;
import org.junit.jupiter.api.*;

import java.io.*;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static test.TestUtils.*;

public class TestFOOL {

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
		compile(code);
		assertFalse(err.ok());
	}

	@Test
	public void incompatible_value_var() throws TypeException {
		String code = """
					let
						var x:bool = 5;
					in
						x;""";
		compile(code);
		assertFalse(err.ok());
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
	public void variable_redefinition() throws TypeException {
		String code = """
					let
						var x:bool = true;
						var x:int = 5;
					in
						x;
				""";
		compile(code);
		assertFalse(err.ok());
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
		compile(code);
		assertFalse(err.ok());
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

	// Object-Oriented tests

	@Test
	public void empty_class() throws TypeException {
		String code = """
					let
						class example() {}
					in
						5;
				""";
		compile(code);
		assertTrue(err.ok());
	}

	@Test
	public void class_with_fields() throws TypeException {
		String code = """
					let
						class example(x:int) {}
					in
						5;
				""";
		compile(code);
		assertTrue(err.ok());
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
		compile(code);
		assertTrue(err.ok());
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
		compile(code);
		assertTrue(err.ok());
	}

	@Test
	public void null_type() throws TypeException {
		String code = """
					let
						class useless() {}
					in
						print(null);
				""";
		String asm = compile(code);
		assertTrue(err.ok());
		assertEquals(run(asm).get(0), "-1");
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
							class example() {};
						in 1;
					in 1;
				""";
		// all of this mess is just to avoid automatic error printing by antlr
		PrintStream old = System.err;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream newps = new PrintStream(baos);
		System.setErr(newps);
		compile(code);  // executing
		newps.flush();  // flushing the output
		System.setErr(old);

		assertFalse(err.ok());
	}

	@Test
	public void class_redefinition() throws TypeException {
		String code = """
					let
						class example() {};
						class example() {};
					in 1;
				""";
		// all of this mess is just to avoid automatic error printing by antlr
		PrintStream old = System.err;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream newps = new PrintStream(baos);
		System.setErr(newps);
		compile(code);  // executing
		newps.flush();  // flushing the output
		System.setErr(old);

		assertFalse(err.ok());
	}

	@Test
	public void object_redefinition() throws TypeException {
		String code = """
					let
						class example() {};
						var x:example = new example();
						var x:example = new example();
					in 1;
				""";
		// all of this mess is just to avoid automatic error printing by antlr
		PrintStream old = System.err;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream newps = new PrintStream(baos);
		System.setErr(newps);
		compile(code);  // executing
		newps.flush();  // flushing the output
		System.setErr(old);

		assertFalse(err.ok());
	}

	@Test
	public void simple_object_usage() throws TypeException {
		String code = """
					let
						class example(x:int) {
							fun getX:int () (x);
						}
						var obj:example = new example(5);
					in
						print(obj.getX());
				""";
		assertEquals(compileAndRun(code).get(0), "5");
	}

	// Object Inheritance tests

	// Code Optimization tests
}
