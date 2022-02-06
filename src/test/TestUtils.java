package test;

import compiler.*;
import compiler.exc.*;
import compiler.lib.*;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import svm.ExecuteVM;
import svm.SVMLexer;
import svm.SVMParser;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;

public class TestUtils {
	public static ArrayList<String> compileAndRun (final String code) throws TypeException {
		return run(compile(code));
	}

	public static String compile (final String code) throws TypeException {
		return compile(code, false);
	}

	public static String compile (final String code, final boolean debug) throws TypeException {
		TestErrors err = TestErrors.getInstance();
		err.reset();

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

		if(debug) System.out.println("Generating code.");
		return new CodeGenerationASTVisitor().visit(ast);
	}

	public static ArrayList<String> run (final String code) {
		return run(code, false);
	}

	public static ArrayList<String> run (final String code, final boolean debug) {
		if(debug) System.out.println("\nAssembling generated code.");
		CharStream charsASM = CharStreams.fromString(code);
		SVMLexer lexerASM = new SVMLexer(charsASM);
		CommonTokenStream tokensASM = new CommonTokenStream(lexerASM);
		SVMParser parserASM = new SVMParser(tokensASM);

		parserASM.assembly();

		// needed only for debug
		if(debug) System.out.println("You had: " + lexerASM.lexicalErrors + " lexical errors and " + parserASM.getNumberOfSyntaxErrors() + " syntax errors.\n");

		if(debug) System.out.println("Running generated code via Stack Virtual Machine.");
		ExecuteVM vm = new ExecuteVM(parserASM.code);

		// before execution we redirect the output on a String
		PrintStream old = System.out;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream newps = new PrintStream(baos);
		System.setOut(newps);
		vm.cpu();  // executing
		newps.flush();  // flushing the output
		System.setOut(old);

		// Using "\r?\n" to be compatible with Windows
		return new ArrayList<>(Arrays.asList(baos.toString().split("\r?\n")));
	}
}