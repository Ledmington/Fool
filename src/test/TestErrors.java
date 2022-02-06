package test;

public class TestErrors {
	public static TestErrors getInstance() {
		if (instance == null) {
			instance = new TestErrors();
		}
		return instance;
	}

	private static TestErrors instance;

	private TestErrors() {}

	public int lexerErrors = 0;
	public int parserErrors = 0;
	public int symTableErrors = 0;
	public int typeErrors = 0;

	public int totalErrors() {
		return lexerErrors + parserErrors + symTableErrors + typeErrors;
	}

	public void reset() {
		lexerErrors = 0;
		parserErrors = 0;
		typeErrors = 0;
	}
}
