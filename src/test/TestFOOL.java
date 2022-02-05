package test;

import compiler.exc.TypeException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static test.TestUtils.compile;
import static test.TestUtils.compileAndRun;

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

		// only booleans allowed in or
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

		// only booleans allowed in and
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
				        );
				      """;
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
				        );
				      """;
		assertEquals(compileAndRun(code).get(0), "0");
	}

	// 2 punti: "<=", ">=", "||", "&&", "/", "-" e "!"
}
