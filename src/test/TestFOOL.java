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
	public void booleansAsInts() throws TypeException {
		assertEquals(compileAndRun("print(true);").get(0), "1");
		assertEquals(compileAndRun("print(false);").get(0), "0");
	}

	@Test
	public void simpleIf() throws TypeException {
		assertEquals(compileAndRun("print(if 2+2 == 3 then {1} else {5});").get(0), "5");
	}

	@Test
	public void simpleVar() throws TypeException {
		assertEquals(compileAndRun("let var x:int = 5; in print(x);").get(0), "5");
		assertEquals(compileAndRun("let var x:bool = true; in print(x);").get(0), "1");
	}

	@Test
	public void simpleFun() throws TypeException {
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
	public void simpleMath() throws TypeException {
		assertEquals(compileAndRun("print(1+2);").get(0), "3");
		assertEquals(compileAndRun("print(2-1);").get(0), "1");
		assertEquals(compileAndRun("print(2*3);").get(0), "6");
		assertEquals(compileAndRun("print(10/5);").get(0), "2");
	}

	@Test
	public void divisionByZero() {
		assertThrows(ArithmeticException.class, () -> compileAndRun("1/0;"));
	}

	@Test
	public void negativeNumbers() throws TypeException {
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
	public void testEq() throws TypeException {
		assertEquals(compileAndRun("print(1==2);").get(0), "0");
		assertEquals(compileAndRun("print(2==2);").get(0), "1");
		assertEquals(compileAndRun("print(3==2);").get(0), "0");
	}

	@Test
	public void testGeq() throws TypeException {
		assertEquals(compileAndRun("print(1>=2);").get(0), "0");
		assertEquals(compileAndRun("print(2>=2);").get(0), "1");
		assertEquals(compileAndRun("print(3>=2);").get(0), "1");
	}

	@Test
	public void testLeq() throws TypeException {
		assertEquals(compileAndRun("print(1<=2);").get(0), "1");
		assertEquals(compileAndRun("print(2<=2);").get(0), "1");
		assertEquals(compileAndRun("print(3<=2);").get(0), "0");
	}

	@Test
	public void testOr() throws TypeException {
		assertEquals(compileAndRun("print(false||false);").get(0), "0");
		assertEquals(compileAndRun("print(false||true);").get(0), "1");
		assertEquals(compileAndRun("print(true||false);").get(0), "1");
		assertEquals(compileAndRun("print(true||true);").get(0), "1");
	}

	@Test
	public void onlyBoolInOr() {
		assertThrows(TypeException.class, () -> compileAndRun("print(3 || 2);"));
		assertThrows(TypeException.class, () -> compileAndRun("print(3 || true);"));
		assertThrows(TypeException.class, () -> compileAndRun("print(true || 2);"));
	}

	@Test
	public void testAnd() throws TypeException {
		assertEquals(compileAndRun("print(false&&false);").get(0), "0");
		assertEquals(compileAndRun("print(false&&true);").get(0), "0");
		assertEquals(compileAndRun("print(true&&false);").get(0), "0");
		assertEquals(compileAndRun("print(true&&true);").get(0), "1");
	}

	@Test
	public void onlyBoolInAnd() {
		assertThrows(TypeException.class, () -> compileAndRun("print(3 && 2);"));
		assertThrows(TypeException.class, () -> compileAndRun("print(3 && true);"));
		assertThrows(TypeException.class, () -> compileAndRun("print(true && 2);"));
	}

	@Test
	public void testNot() throws TypeException {
		assertEquals(compileAndRun("print(!false);").get(0), "1");
		assertEquals(compileAndRun("print(!true);").get(0), "0");
	}

	@Test
	public void onlyBoolInNot() {
		assertThrows(TypeException.class, () -> compileAndRun("print(!0);"));
		assertThrows(TypeException.class, () -> compileAndRun("print(!1);"));
		assertThrows(TypeException.class, () -> compileAndRun("print(!2);"));
	}

	@Test
	public void efficientOr() throws TypeException {
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
	public void efficientAnd() throws TypeException {
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
	public void wrongReturnTypeFunction() throws TypeException {
		String code = """
					let
						fun f:bool() let var x:int=5; in x;
					in
						f();""";
		compiler.compileSource(code);
		assertFalse(compiler.err.ok());
	}

	@Test
	public void incompatibleValueVar() throws TypeException {
		String code = """
					let
						var x:bool = 5;
					in
						x;""";
		compiler.compileSource(code);
		assertFalse(compiler.err.ok());
	}

	@Test
	public void nonBoolInIf() {
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
	public void differentTypesInIf() {
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
	public void incompatibleTypesInEqual() {
		String code = """
					let
						fun f:int() (5);
					in
						if (true==f) then {1} else {0};
				""";
		assertThrows(TypeException.class, () -> compile(code));
	}

	@Test
	public void incompatibleTypesInGreaterequal() {
		String code = """
					let
						fun f:int() (5);
					in
						if (true>=f) then {1} else {0};
				""";
		assertThrows(TypeException.class, () -> compile(code));
	}

	@Test
	public void incompatibleTypesInLessequal() {
		String code = """
					let
						fun f:int() (5);
					in
						if (true==f) then {1} else {0};
				""";
		assertThrows(TypeException.class, () -> compile(code));
	}

	@Test
	public void onlyIntInMul() {
		String code = """
					let
						fun f:int() (5);
					in
						5*f;
				""";
		assertThrows(TypeException.class, () -> compile(code));
	}

	@Test
	public void onlyIntInDiv() {
		String code = """
					let
						fun f:int() (5);
					in
						f/5;
				""";
		assertThrows(TypeException.class, () -> compile(code));
	}

	@Test
	public void onlyIntInSum() {
		String code = """
					let
						fun f:int() (5);
					in
						5+f;
				""";
		assertThrows(TypeException.class, () -> compile(code));
	}

	@Test
	public void onlyIntInSub() {
		String code = """
					let
						fun f:int() (5);
					in
						5-f;
				""";
		assertThrows(TypeException.class, () -> compile(code));
	}

	@Test
	public void nonFunctionInvocation() {
		String code = """
					let
						var x:int = 5;
					in
						x();
				""";
		assertThrows(TypeException.class, () -> compile(code));
	}

	@Test
	public void wrongNumberOfParameters() {
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
	public void wrongParameterType() {
		String code = """
					let
						fun f:int(x:bool) (5);
					in
						f(5);
				""";
		assertThrows(TypeException.class, () -> compile(code));
	}

	@Test
	public void unexistingVariable() {
		String code = """
					x;
				""";
		assertThrows(IncomplException.class, () -> compile(code));
	}

	@Test
	public void variableRedefinition() throws TypeException {
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
	public void functionRedefinition() throws TypeException {
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
	public void functionArgumentRedefinition() {
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
	public void factorial() throws TypeException {
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
	public void euclidGCD() throws TypeException {
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
	public void squareRoot() throws TypeException {
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
	public void squareRootBinarySearch() throws TypeException {
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
	public void collatzConjecture() throws TypeException {
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
	public void emptyClass() throws TypeException {
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
	public void classWithFields() throws TypeException {
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
	public void classWithMethods() throws TypeException {
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
	public void classWithFieldsAndMethods() throws TypeException {
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
	public void nullType() throws TypeException {
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
	public void nullIsNotInt() {
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
	public void classesOnlyInGlobalScope() throws TypeException {
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
	public void classRedefinition() throws TypeException {
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
	public void objectRedefinition() throws TypeException {
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
	public void fieldRedefinition() throws TypeException {
		String code = """
					let
						class example(x:int, x:bool) {}
					in 1;
				""";
		compiler.quiet().compileSource(code);
		assertFalse(compiler.err.ok());
	}

	@Test
	public void methodRedefinition() throws TypeException {
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
	public void methodArgumentRedefinition() throws TypeException {
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
	public void cannotAccessFields() {
		String code = """
					let
						class example(a:int) {}
						var x:example = new example(5);
					in x.a;
				""";
		assertThrows(TypeException.class, () -> compiler.quiet().compileSource(code));
	}

	@Test
	public void cannotCallFields() {
		String code = """
					let
						class example(a:int) {}
						var x:example = new example(5);
					in x.a();
				""";
		assertThrows(TypeException.class, () -> compiler.quiet().compileSource(code));
		assertTrue(compiler.err.ok());
	}

	@Test
	public void usingMethodLikeField() throws TypeException {
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
	public void unexistingClass() throws TypeException {
		String code = """
					let
						var x:example = new example();
					in 1;
				""";
		compiler.quiet().compileSource(code);
		assertFalse(compiler.err.ok());
	}

	@Test
	public void callingMethodOnUnexistingObject() throws TypeException {
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
	public void callingUnexistingMethod() throws TypeException {
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
	public void callingMethodOnWrongClass() throws TypeException {
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
	public void functionReturningObject() throws TypeException {
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
	public void methodReturningObject() throws TypeException {
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
	public void nullSubtypeOfClass() throws TypeException {
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
	public void lessFieldsThanRequired() throws TypeException {
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
	public void moreFieldsThanRequired() throws TypeException {
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
	public void wrongFieldType() throws TypeException {
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
	public void methodCall() throws TypeException {
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
	public void methodCallWithTwoClasses() throws TypeException {
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
	public void getterWithThreeFields() throws TypeException {
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
	public void multipleObjects() throws TypeException {
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
	public void multipleClassesMultipleObjects() throws TypeException {
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
						var hello:useless = new useless(false, false);
						var two:example = new example(2);
						var wow:useless = new useless(false, true);
						var three:example = new example(3);
						var ciao:useless = new useless(true, false);
					in
						print(two.getX());
				""";
		String result = compiler.compileSourceAndRun(code).get(0);
		assertTrue(compiler.err.ok());
		assertEquals(result, "2");
	}

	@Test
	public void objectsAsFunctionParameters() throws TypeException {
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

	@Test
	public void assertTypeCheckingInsideMethods() {
		String code = """
					let
						class father() {
							fun m:bool() (
							let
								var a:father = null;
								var b:bool = true;
							in a == b;
							);
						}
					in 1;
				""";
		assertThrows(TypeException.class, () -> compiler.quiet().compileSource(code));
	}

	// Object Inheritance compilation tests

	@Test
	public void subtypingClass() throws TypeException {
		String code = """
					let
						class father() {}
						class example extends father() {}
						fun f:bool(obj:father) (true);
						var x:example = new example();
					in f(x);
				""";
		compiler.compileSource(code);
		assertTrue(compiler.err.ok());
	}

	@Test
	public void unexistingSuperclass() {
		String code = """
					let
						class example extends father() {}
					in 1;
				""";
		assertThrows(NullPointerException.class, () -> compiler.compileSource(code));
	}

	@Test
	public void accessingFatherFields() throws TypeException {
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
	public void accessingFatherMethods() throws TypeException {
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
	public void fieldOverriding() throws TypeException {
		String code = """
					let
						class father(a:int) {
							fun getA:int() (a);
						}
						class example extends father(a:bool, b:int) {
							fun getB:int() (b);
							fun getChildA:bool() (a);
						}
						var x:example = new example(true, 5);
					in print(x.getChildA());
				""";
		compiler.compileSource(code);
		assertTrue(compiler.err.ok());
	}

	@Test
	public void methodOverriding() throws TypeException {
		String code = """
					let
						class father() {
							fun m:int() (5);
						}
						class example extends father() {
							fun m:bool() (true);
						}
						var x:example = new example();
					in print(x.m());
				""";
		compiler.compileSource(code);
		assertTrue(compiler.err.ok());
	}

	@Test
	public void uselessOverriding1() throws TypeException {
		String code = """
					let
						class father() {
							fun m:int() (5);
						}
						class example extends father() {
							fun m:int() (7);
						}
						var x:example = new example();
					in print(x.m());
				""";
		compiler.compileSource(code);
		assertTrue(compiler.err.ok());
	}

	@Test
	public void uselessOverriding2() throws TypeException {
		String code = """
					let
						class father() {
							fun m:int(a:bool) (5);
						}
						class example extends father() {
							fun m:int(a:bool) (7);
						}
						var x:example = new example();
					in print(x.m(true));
				""";
		compiler.compileSource(code);
		assertTrue(compiler.err.ok());
	}

	@Test
	public void uselessOverriding3() throws TypeException {
		String code = """
					let
						class father() {
							fun m:int(a:father, b:int) (5);
						}
						class example extends father() {
							fun m:int(a:father, b:int) (7);
						}
						var f:father = new father();
						var x:example = new example();
					in print(x.m(f,55));
				""";
		compiler.compileSource(code);
		assertTrue(compiler.err.ok());
	}

	@Test
	public void wrongFieldOverriding() throws TypeException {
		String code = """
					let
						class father(x:bool) {}
						class example extends father(x:int) {}
					in 1;
				""";
		compiler.compileSource(code);
		assertFalse(compiler.err.ok());
	}

	@Test
	public void wrongMethodOverriding1() throws TypeException {
		String code = """
					let
						class father() {
							fun m:bool() (true);
						}
						class example extends father() {
							fun m:int() (5);
						}
					in 1;
				""";
		compiler.compileSource(code);
		assertFalse(compiler.err.ok());
	}

	@Test
	public void wrongMethodOverriding2() throws TypeException {
		String code = """
					let
						class father() {
							fun m:int(a:int) (5);
						}
						class example extends father() {
							fun m:int(a:bool) (5);
						}
					in 1;
				""";
		compiler.compileSource(code);
		assertFalse(compiler.err.ok());
	}

	@Test
	public void wrongMethodOverriding3() throws TypeException {
		String code = """
					let
						class father() {
							fun m:int(a:bool) (5);
						}
						class example extends father() {
							fun m:int() (5);
						}
					in 1;
				""";
		compiler.compileSource(code);
		assertFalse(compiler.err.ok());
	}

	@Test
	public void wrongMethodOverriding4() throws TypeException {
		String code = """
					let
						class father() {
							fun m:int(a:bool) (5);
						}
						class example extends father() {
							fun m:int(a:int, b:int) (5);
						}
					in 1;
				""";
		compiler.compileSource(code);
		assertFalse(compiler.err.ok());
	}

	@Test
	public void methodCallOnWrongClass() throws TypeException {
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

	@Test
	public void methodCallOnGrandfatherClass() throws TypeException {
		String code = """
					let
						class grandfather() {
							fun m:int() (5);
						}
						class father extends grandfather() {}
						class example extends father() {}
						var x:example = new example();
					in x.m();
				""";
		compiler.compileSource(code);
		assertTrue(compiler.err.ok());
	}

	@Test
	public void methodOverridingField() throws TypeException {
		String code = """
						let
							class father(m:int) {}
							class example extends father() {
								fun m:int() (5);
							}
							var x:example = new example();
						in 1;
				""";
		compiler.compileSource(code);
		assertFalse(compiler.err.ok());
	}

	@Test
	public void fieldOverridingMethod() throws TypeException {
		String code = """
						let
							class father() {
								fun m:int() (5);
							}
							class example extends father(m:int) {}
							var x:example = new example();
						in 1;
				""";
		compiler.compileSource(code);
		assertFalse(compiler.err.ok());
	}

	// Object Inheritance execution tests

	@Test
	public void subclassAsParameter() throws TypeException {
		String code = """
					let
						class father() {}
						class example extends father() {}
						fun f:int(obj:father) (5);
						var x:example = new example();
					in print(f(x));
				""";
		String result = compiler.compileSourceAndRun(code).get(0);
		assertTrue(compiler.err.ok());
		assertEquals(result, "5");
	}

	@Test
	public void subsubclassAsParameter() throws TypeException {
		String code = """
					let
						class grandfather() {}
						class father extends grandfather() {}
						class example extends father() {}
						fun f:int(obj:grandfather) (5);
						var x:example = new example();
					in print(f(x));
				""";
		String result = compiler.compileSourceAndRun(code).get(0);
		assertTrue(compiler.err.ok());
		assertEquals(result, "5");
	}

	@Test
	public void superclassGetter() throws TypeException {
		String code = """
					let
						class father(a:int) {
							fun m:int() (a);
						}
						class example extends father() {}
						var x:example = new example(6);
					in print(x.m());
				""";
		String result = compiler.compileSourceAndRun(code).get(0);
		assertTrue(compiler.err.ok());
		assertEquals(result, "6");
	}

	@Test
	public void supersuperclassGetter() throws TypeException {
		String code = """
					let
						class grandfather(a:int) {
							fun m:int() (a);
						}
						class father extends grandfather() {}
						class example extends father() {}
						var x:example = new example(6);
					in print(x.m());
				""";
		String result = compiler.compileSourceAndRun(code).get(0);
		assertTrue(compiler.err.ok());
		assertEquals(result, "6");
	}

	@Test
	public void superclassMethod() throws TypeException {
		String code = """
					let
						class father() {
							fun m:int() (7);
						}
						class example extends father() {}
						var x:example = new example();
					in print(x.m());
				""";
		String result = compiler.compileSourceAndRun(code).get(0);
		assertTrue(compiler.err.ok());
		assertEquals(result, "7");
	}

	@Test
	public void supersuperclassMethod() throws TypeException {
		String code = """
					let
						class grandfather() {
							fun m:int() (7);
						}
						class father extends grandfather() {}
						class example extends father() {}
						var x:example = new example();
					in print(x.m());
				""";
		String result = compiler.compileSourceAndRun(code).get(0);
		assertTrue(compiler.err.ok());
		assertEquals(result, "7");
	}

	@Test
	public void fieldOverride() throws TypeException {
		String code = """
					let
						class father(a:int) {
							fun m:int() (a);
						}
						class example extends father(a:bool) {}
						var x:example = new example(true);
					in print(x.m());
				""";
		String result = compiler.compileSourceAndRun(code).get(0);
		assertTrue(compiler.err.ok());
		assertEquals(result, "1");
	}

	@Test
	public void doubleFieldOverride() throws TypeException {
		String code = """
					let
						class grandfather() {}
						class father extends grandfather() {}
						class son extends father() {}
						
						class supersuperexample(a:grandfather) {
							fun m:int() (7);
						}
						class superexample extends supersuperexample(a:father) {}
						class example extends superexample(a:son) {}
						var obj:son = new son();
						var x:example = new example(obj);
					in print(x.m());
				""";
		String result = compiler.compileSourceAndRun(code).get(0);
		assertTrue(compiler.err.ok());
		assertEquals(result, "7");
	}

	@Test
	public void supersuperclassFieldOverride() throws TypeException {
		String code = """
					let
						class grandfather(a:int) {
							fun m:int() (a);
						}
						class father extends grandfather(a:int) {}
						class example extends father(a:bool) {}
						var x:example = new example(true);
					in print(x.m());
				""";
		String result = compiler.compileSourceAndRun(code).get(0);
		assertTrue(compiler.err.ok());
		assertEquals(result, "1");
	}

	@Test
	public void methodOverride() throws TypeException {
		String code = """
					let
						class father() {
							fun m:int() (7);
						}
						class example extends father() {
							fun m:bool() (true);
						}
						var x:example = new example();
					in print(x.m());
				""";
		String result = compiler.compileSourceAndRun(code).get(0);
		assertTrue(compiler.err.ok());
		assertEquals(result, "1");
	}

	@Test
	public void doubleMethodOverride() throws TypeException {
		String code = """
					let
						class grandfather() {
							fun x:int() (7);
						}
						class father extends grandfather() {}
						class son extends father() {}
						
						class supersuperexample() {
							fun m:grandfather() (new grandfather());
						}
						class superexample extends supersuperexample() {
							fun m:father() (new father());
						}
						class example extends superexample() {
							fun m:son() (new son());
						}
						var x:example = new example();
						var obj:son = x.m();
					in print(obj.x());
				""";
		String result = compiler.compileSourceAndRun(code).get(0);
		assertTrue(compiler.err.ok());
		assertEquals(result, "7");
	}

	@Test
	public void supersuperclassMethodOverride() throws TypeException {
		String code = """
					let
						class grandfather() {
							fun m:int() (7);
						}
						class father extends grandfather() {}
						class example extends father() {
							fun m:bool() (true);
						}
						var x:example = new example();
					in print(x.m());
				""";
		String result = compiler.compileSourceAndRun(code).get(0);
		assertTrue(compiler.err.ok());
		assertEquals(result, "1");
	}

	@Test
	public void uselessOverrideRettype() throws TypeException {
		String code = """
					let
						class father() {
							fun m:int() (7);
						}
						class example extends father() {
							fun m:int() (7);
						}
						var x:example = new example();
					in print(x.m());
				""";
		String result = compiler.compileSourceAndRun(code).get(0);
		assertTrue(compiler.err.ok());
		assertEquals(result, "7");
	}

	@Test
	public void uselessOverrideRettypeOneParam() throws TypeException {
		String code = """
					let
						class father() {
							fun m:int(a:int) (7);
						}
						class example extends father() {
							fun m:int(a:int) (7);
						}
						var x:example = new example();
					in print(x.m(5));
				""";
		String result = compiler.compileSourceAndRun(code).get(0);
		assertTrue(compiler.err.ok());
		assertEquals(result, "7");
	}

	@Test
	public void uselessOverrideRettypeTwoParams() throws TypeException {
		String code = """
					let
						class father() {
							fun m:int(a:int, b:bool) (7);
						}
						class example extends father() {
							fun m:int(a:int, b:bool) (7);
						}
						var x:example = new example();
					in print(x.m(5, false));
				""";
		String result = compiler.compileSourceAndRun(code).get(0);
		assertTrue(compiler.err.ok());
		assertEquals(result, "7");
	}

	@Test
	public void constructorWithImplicitFields() throws TypeException {
		String code = """
					let
						class father(a:int) {
							fun getA:int() (a);
						}
						class example extends father(b:int) {
							fun getB:int() (b);
						}
						var x:example = new example(5,6);
					in print(x.getA());
				""";
		String result = compiler.compileSourceAndRun(code).get(0);
		assertTrue(compiler.err.ok());
		assertEquals(result, "5");
	}

	@Test
	public void constructorWithImplicitFieldsOnlyGrandfather() throws TypeException {
		String code = """
					let
						class grandfather(a:int) {
							fun getA:int() (a);
						}
						class father extends grandfather() {}
						class example extends father(b:int) {
							fun getB:int() (b);
						}
						var x:example = new example(5,6);
					in print(x.getA());
				""";
		String result = compiler.compileSourceAndRun(code).get(0);
		assertTrue(compiler.err.ok());
		assertEquals(result, "5");
	}

	@Test
	public void constructorWithImplicitFieldsFatherAndGrandfather() throws TypeException {
		String code = """
					let
						class grandfather(a:int) {
							fun getA:int() (a);
						}
						class father extends grandfather(b:int) {
							fun getB:int() (b);
						}
						class example extends father(c:int) {
							fun getC:int() (c);
						}
						var x:example = new example(5,6,7);
					in print(x.getA());
				""";
		String result = compiler.compileSourceAndRun(code).get(0);
		assertTrue(compiler.err.ok());
		assertEquals(result, "5");
	}

	@Test
	public void bankloan() throws TypeException {
		String code = """
					let
				 
				   class Account (money:int) {
				     fun getMon:int () money;
				   }
				  
				   class TradingAcc extends Account (invested:int) {
				     fun getInv:int () invested;
				   }
				  
				   class BankLoan (loan: Account) {
				     fun getLoan:Account () loan;
				     fun openLoan:Account (m:TradingAcc) (
				     	if ((m.getMon()+m.getInv()) >= 30000) then {
				     		new Account(loan.getMon())
				     	} else {
				     		null
				     	}
				     );
				   }
				  
				   class MyBankLoan extends BankLoan (loan: TradingAcc) {
				     fun openLoan:TradingAcc (l:Account) (
				     	if (l.getMon() >= 20000) then {
				     		new TradingAcc(loan.getMon(), loan.getInv())
				     	} else {
				     		null
				     	}
				     );
				   }
				   
				   var bl:BankLoan = new MyBankLoan(new TradingAcc(50000, 40000));
				   var myTradingAcc:TradingAcc = new TradingAcc(20000, 5000);
				   var myLoan:Account = bl.openLoan(myTradingAcc);
				  
				 in print(if (myLoan==null) then {0} else {myLoan.getMon()});
				""";
		List<String> result = compiler.compileSourceAndRun(code);
		assertTrue(compiler.err.ok());
		assertEquals(result, List.of("50000"));
	}

	@Test
	public void methodOverrideInOtherOrder() throws TypeException {
		String code = """
						let
							class father() {
								fun m:int() (7);
							}
							class example extends father() {
								fun wow:int() (6);
								fun m:bool() (true);
							}
							var x:example = new example();
						in print(x.m());
				""";
		String result = compiler.compileSourceAndRun(code).get(0);
		assertTrue(compiler.err.ok());
		assertEquals(result, "1");
	}

	@Test
	public void methodOverrideComplicated() throws TypeException {
		String code = """
						let
							class father() {
								fun a:int() (13);
								fun b:int() (7);
								fun c:int() (29);
							}
							class example extends father() {
								fun x:int() (123);
								fun c:int() (31);
								fun y:int() (99);
								fun a:int() (91);
							}
							var x:example = new example();
						in
							if (print(x.c()) >= 0) then {
								print(x.a())
							} else {
								print(-1)
							};
				""";
		List<String> result = compiler.compileSourceAndRun(code);
		assertTrue(compiler.err.ok());
		assertEquals(result, List.of("31", "91"));
	}

	// Lowest Common Ancestor testing

	@Test
	public void lowestCommonAncestorWithClasses1() throws TypeException {
		String code = """
						let
							class father() {
								fun m:int() (55);
							}
							class leftson extends father() {
								fun m:int() (22);
							}
							class rightson extends father() {
								fun m:int() (99);
							}
							fun f:father() (
								if (true) then {
									new leftson()
								} else {
									new rightson()
								}
							);
							var x:father = f();
						in
							print(x.m());
				""";
		List<String> result = compiler.compileSourceAndRun(code);
		assertTrue(compiler.err.ok());
		assertEquals(result, List.of("22"));
	}

	@Test
	public void lowestCommonAncestorWithClasses2() throws TypeException {
		String code = """
						let
							class father() {
								fun m:int() (55);
							}
							class leftson extends father() {
								fun m:int() (22);
							}
							class rightson extends father() {
								fun m:int() (99);
							}
							fun f:father() (
								if (true) then {
									new rightson()
								} else {
									new leftson()
								}
							);
							var x:father = f();
						in
							print(x.m());
				""";
		List<String> result = compiler.compileSourceAndRun(code);
		assertTrue(compiler.err.ok());
		assertEquals(result, List.of("99"));
	}

	@Test
	public void lowestCommonAncestorWithIntAndBool1() throws TypeException {
		String code = """
						let
							var x:int = 5;
							var b:bool = false;
						in
							print(if (true) then {
								x
							} else {
								b
							});
				""";
		List<String> result = compiler.compileSourceAndRun(code);
		assertTrue(compiler.err.ok());
		assertEquals(result, List.of("5"));
	}

	@Test
	public void lowestCommonAncestorWithIntAndBool2() throws TypeException {
		String code = """
						let
							var x:int = 5;
							var b:bool = false;
						in
							print(if (true) then {
								b
							} else {
								x
							});
				""";
		List<String> result = compiler.compileSourceAndRun(code);
		assertTrue(compiler.err.ok());
		assertEquals(result, List.of("0"));
	}

	@Test
	public void listif() throws TypeException {
		String code = """
						let
				  
				      class List (){
				          fun add:bool(f:int) false;
				          fun get:int(i:int) 3;
				      }
				  
				      class LinkedList extends List (f:int, r:List)
				      {
				        fun get:int(i:int) 5;
				      }
				  
				      class ArrayList extends List (size:int)
				      {
				        fun get:int(i:int) 7;
				      }
				                                                         \s
				      fun list:List()
				          if(true)
				          then{ new LinkedList(33, null)}
				          else{ new ArrayList(55)};
				  
				    var l: List = list();
				  in print(l.get(0));
				""";
		List<String> result = compiler.compileSourceAndRun(code);
		assertTrue(compiler.err.ok());
		assertEquals(result, List.of("5"));
	}
}