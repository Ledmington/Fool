package test;

import compiler.exc.TypeException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
	}

	@Test
	public void test_and() throws TypeException {
		assertEquals(compileAndRun("print(false&&false);").get(0), "0");
		assertEquals(compileAndRun("print(false&&true);").get(0), "0");
		assertEquals(compileAndRun("print(true&&false);").get(0), "0");
		assertEquals(compileAndRun("print(true&&true);").get(0), "1");
	}

	@Test
	public void test_not() throws TypeException {
		assertEquals(compileAndRun("print(!false);").get(0), "1");
		assertEquals(compileAndRun("print(!true);").get(0), "0");
	}

	// 2 punti: "<=", ">=", "||", "&&", "/", "-" e "!"
}
