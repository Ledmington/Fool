package fool;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.*;

public class Test {
    public static void main(String[] args) throws Exception {

        String fileName = "prova.txt";

        CharStream chars = CharStreams.fromFileName(fileName);
        exp.SimpleExpLexer lexer = new exp.SimpleExpLexer(chars);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        exp.SimpleExpParser parser = new exp.SimpleExpParser(tokens);
        ParseTree prog = parser.prog();

        if(lexer.lexicalErrors + parser.getNumberOfSyntaxErrors() > 0) {
            System.out.println("Lexical errors: " + lexer.lexicalErrors);
            System.out.println("Syntax errors: " + parser.getNumberOfSyntaxErrors());
        }
        else {
            System.out.println("Calculating result...");
            SimpleCalcSTVisitor visitor = new SimpleCalcSTVisitor();
            int result = visitor.visit(prog);
            System.out.println("\nResult: " + result);
        }
    }
}
