package test;

import compiler.exc.TypeException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static test.TestUtils.compileAndRun;

public class TestFOOL {

	@Test
	public void simple() throws TypeException {
		int result = Integer.parseInt(compileAndRun("print(3+1);").get(0));
		assertEquals(result, 4);
	}
}
