package test;

import compiler.*;
import compiler.exc.*;
import compiler.lib.*;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import svm.ExecuteVM;
import visualsvm.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class TestUtils {

	public TestErrors err;

	private boolean debug = false;  // if true prints on console the errors and ASTs
	private boolean quiet = false;  // if true hides the automatic error printing by ANTLR
	private boolean visual = false; // if true uses the visualSVM to run the code

	public TestUtils debug() {
		this.debug = true;
		return this;
	}

	public TestUtils quiet() {
		this.quiet = true;
		return this;
	}

	public TestUtils visual() {
		this.visual = true;
		return this;
	}

	public List<String> compileSourceAndRun (final String code) throws TypeException {
		CharStream charsASM = CharStreams.fromString(compileSource(code));
		SVMLexer lexerASM = new SVMLexer(charsASM);
		CommonTokenStream tokensASM = new CommonTokenStream(lexerASM);
		SVMParser parserASM = new SVMParser(tokensASM);

		parserASM.assembly();

		ByteArrayOutputStream baos = new ByteArrayOutputStream();;

		List<String> source = new LinkedList<>();
		for(String line : code.split("\r?\n")) {
			source.add(line);
		}

		if(visual) {
			visualsvm.ExecuteVM vm = new visualsvm.ExecuteVM(parserASM.code, parserASM.sourceMap, source);
			vm.cpu();
		} else {
			ExecuteVM vm = new ExecuteVM(parserASM.code);
			PrintStream old = System.out;
			PrintStream newps = new PrintStream(baos);
			System.setOut(newps);
			vm.cpu();  // executing
			newps.flush();  // flushing the output
			System.setOut(old);
		}

		// Using "\r?\n" to be compatible with Windows
		return Arrays.stream(baos.toString().split("\r?\n")).toList();
	}

	public String compileSource (final String code) throws TypeException {
		PrintStream old = null;
		PrintStream newps = null;
		if(quiet) {
			old = System.err;
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			newps = new PrintStream(baos);
			System.setErr(newps);
		}

		err = new TestErrors();
		FOOLlib.typeErrors = 0;

		CharStream chars = CharStreams.fromString(code);
		FOOLLexer lexer = new FOOLLexer(chars);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		FOOLParser parser = new FOOLParser(tokens);

		if(debug) System.out.println("Generating ST via lexer and parser.");
		ParseTree st = parser.prog();
		if(debug) System.out.println("You had " + lexer.lexicalErrors + " lexical errors and " + parser.getNumberOfSyntaxErrors() + " syntax errors.\n");

		if(debug) System.out.println("Generating AST.");
		ASTGenerationSTVisitor visitor = new ASTGenerationSTVisitor(); // use true to visualize the ST
		Node ast = visitor.visit(st);

		if(debug) System.out.println("\nEnriching AST via symbol table.");
		SymbolTableASTVisitor symtableVisitor = new SymbolTableASTVisitor();
		symtableVisitor.visit(ast);
		if(debug) System.out.println("You had " + symtableVisitor.stErrors + " symbol table errors.\n");

		if(debug) {
			System.out.println("Visualizing Enriched AST.");
			new PrintEASTVisitor().visit(ast);
		}

		if(debug) System.out.println("\nChecking Types.");
		TypeCheckEASTVisitor typeCheckVisitor = new TypeCheckEASTVisitor();
		TypeNode mainType = typeCheckVisitor.visit(ast);
		if(debug) {
			System.out.print("Type of main program expression is: ");
			new PrintEASTVisitor().visit(mainType);
			System.out.println("You had " + FOOLlib.typeErrors + " type checking errors.\n");
		}

		int frontEndErrors = lexer.lexicalErrors + parser.getNumberOfSyntaxErrors() + symtableVisitor.stErrors + FOOLlib.typeErrors;
		if(debug) System.out.println("You had a total of " + frontEndErrors + " front-end errors.\n");

		err.lexerErrors = lexer.lexicalErrors;
		err.parserErrors = parser.getNumberOfSyntaxErrors();
		err.symTableErrors = symtableVisitor.stErrors;
		err.typeErrors = FOOLlib.typeErrors;

		if(frontEndErrors > 0) return null; // make the test fail if compilation failed

		if(debug) System.out.println("Generating code.");

		if(quiet) {
			assert newps != null;
			newps.flush();  // flushing the output
			System.setErr(old);
		}

		return new CodeGenerationASTVisitor().visit(ast);
	}

	public static String compile(final String code) throws TypeException {
		return new TestUtils().compileSource(code);
	}

	public static List<String> compileAndRun(final String code) throws TypeException {
		return new TestUtils().compileSourceAndRun(code);
	}
}