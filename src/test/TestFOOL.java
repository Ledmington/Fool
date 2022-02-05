package test;

import compiler.exc.TypeException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static test.TestUtils.compileAndRun;

public class TestFOOL {

	@Test
	public void simple() throws TypeException {
		compileAndRun("3+true;");
	}
}
