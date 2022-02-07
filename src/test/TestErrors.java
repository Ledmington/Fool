package test;

public class TestErrors {

	public int lexerErrors = 0;
	public int parserErrors = 0;
	public int symTableErrors = 0;
	public int typeErrors = 0;

	public int totalErrors() {
		return lexerErrors + parserErrors + symTableErrors + typeErrors;
	}

	public boolean ok() {
		return totalErrors() == 0;
	}
}
