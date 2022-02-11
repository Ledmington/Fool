package compiler;

import compiler.exc.*;
import compiler.lib.*;
import compiler.AST.*;
import static compiler.lib.FOOLlib.*;
import static svm.ExecuteVM.*;

public class CodeGenerationASTVisitor extends BaseASTVisitor<String, VoidException> {

	public CodeGenerationASTVisitor() {}
	CodeGenerationASTVisitor(boolean debug) {super(false, debug);} //enables print for debugging

	@Override
	public String visitNode(ProgLetInNode n) {
		if (print) printNode(n);
		String declCode = null;
		for (Node dec : n.declist) declCode = nlJoin(declCode, visit(dec));
		return nlJoin(
			"push 0",	
			declCode, // generate code for declarations (allocation)			
			visit(n.exp),
			"halt",
			getCode()
		);
	}

	@Override
	public String visitNode(ProgNode n) {
		if (print) printNode(n);
		return nlJoin(
			visit(n.exp),
			"halt"
		);
	}

	@Override
	public String visitNode(FunNode n) {
		if (print) printNode(n,n.id);

		String declCode = null, popDecl = null, popParl = null;

		for (Node dec : n.declist) {
			declCode = nlJoin(declCode, visit(dec));
			popDecl = nlJoin(popDecl, "pop");
		}

		for (int i=0; i<n.parlist.size(); i++) {
			popParl = nlJoin(popParl, "pop");
		}

		String funl = freshFunLabel();

		putCode(
			nlJoin(
					"/* "+n.id+" */",
				funl+":",
				"cfp", // set $fp to $sp value
				"lra", // load $ra value
				"/* local declaration code */",
				declCode, // generate code for local declarations (they use the new $fp!!!)
				"/* function body */",
				visit(n.exp), // generate code for function body expression
				"stm", // set $tm to popped value (function result)
				"/* removing local declaration */",
				popDecl, // remove local declarations from stack
				"sra", // set $ra to popped value
				"pop", // remove Access Link from stack
				"/* removing parameters */",
				popParl, // remove parameters from stack
				"sfp", // set $fp to popped value (Control Link)
				"ltm", // load $tm value (function result)
				"lra", // load $ra value
				"js"  // jump to to popped address
			)
		);

		return "push "+funl;		
	}

	@Override
	public String visitNode(VarNode n) {
		if (print) printNode(n,n.id);
		return visit(n.exp);
	}

	@Override
	public String visitNode(PrintNode n) {
		if (print) printNode(n);
		return nlJoin(
			visit(n.exp),
			"print"
		);
	}

	@Override
	public String visitNode(IfNode n) {
		if (print) printNode(n);
	 	String l1 = freshLabel();
	 	String l2 = freshLabel();		
		return nlJoin(
			visit(n.cond),
			"push 1",
			"beq "+l1,
			visit(n.el),
			"b "+l2,
			l1+":",
			visit(n.th),
			l2+":"
		);
	}

	@Override
	public String visitNode(EqualNode n) {
		if (print) printNode(n);
	 	String l1 = freshLabel();
	 	String l2 = freshLabel();
		return nlJoin(
			visit(n.left),
			visit(n.right),
			"beq "+l1,
			"push 0",
			"b "+l2,
			l1+":",
			"push 1",
			l2+":"
		);
	}

	@Override
	public String visitNode(LessEqualNode n) {
		if (print) printNode(n);
		String l1 = freshLabel();
		String l2 = freshLabel();
		return nlJoin(
				visit(n.left),
				visit(n.right),
				"bleq "+l1,
				"push 0",
				"b "+l2,
				l1+":",
				"push 1",
				l2+":"
		);
	}

	@Override
	public String visitNode(GreaterEqualNode n) {
		if (print) printNode(n);
		String l1 = freshLabel();
		String l2 = freshLabel();
		return nlJoin(
				// since we can only check if "less or equal", we evaluate expressions in reverse order and check if less or equal
				visit(n.right),
				visit(n.left),
				"bleq "+l1,
				"push 0",
				"b "+l2,
				l1+":",
				"push 1",
				l2+":"
		);
	}

	@Override
	public String visitNode(OrNode n) {
		if (print) printNode(n);
		String labelTrue = freshLabel();
		String labelEnd = freshLabel();
		return nlJoin(
				visit(n.left),
				"push 1",
				"beq "+labelTrue,
				visit(n.right),
				"push 1",
				"beq "+labelTrue,
				"push 0",
				"b "+labelEnd,
				labelTrue+":",
				"push 1",
				labelEnd+":"
		);
	}

	@Override
	public String visitNode(AndNode n) {
		if (print) printNode(n);
		String labelFalse = freshLabel();
		String labelEnd = freshLabel();
		return nlJoin(
				visit(n.left),
				"push 0",
				"beq " + labelFalse,
				visit(n.right),
				"push 0",
				"beq " + labelFalse,
				"push 1",
				"b " + labelEnd,
				labelFalse + ":",
				"push 0",
				labelEnd + ":"
		);
	}

	@Override
	public String visitNode(NotNode n) {
		if (print) printNode(n);
		return nlJoin(
				"push 1",
				visit(n.exp),
				"sub"
		);
	}

	@Override
	public String visitNode(TimesNode n) {
		if (print) printNode(n);
		return nlJoin(
			visit(n.left),
			visit(n.right),
			"mult"
		);	
	}

	@Override
	public String visitNode(DivNode n) {
		if (print) printNode(n);
		return nlJoin(
				visit(n.left),
				visit(n.right),
				"div"
		);
	}

	@Override
	public String visitNode(PlusNode n) {
		if (print) printNode(n);
		return nlJoin(
			visit(n.left),
			visit(n.right),
			"add"				
		);
	}

	@Override
	public String visitNode(MinusNode n) {
		if (print) printNode(n);
		return nlJoin(
				visit(n.left),
				visit(n.right),
				"sub"
		);
	}

	@Override
	public String visitNode(CallNode n) {
		if (print) printNode(n,n.id);

		String argCode = null, getAR = null;

		for (int i=n.arglist.size()-1; i>=0; i--) {
			argCode = nlJoin(argCode, visit(n.arglist.get(i)));
		}

		for (int i = 0; i<n.nl-n.entry.nl; i++) {
			getAR = nlJoin(getAR, "lw");
		}

		return nlJoin(
			"/* calling "+n.id+" */",
			"lfp", // load Control Link (pointer to frame of function "id" caller)
			"/* argument expressions */",
			argCode, // generate code for argument expressions in reversed order
			"/* searching "+n.id+" declaration */",
			"lfp", getAR, // retrieve address of frame containing "id" declaration
                          // by following the static chain (of Access Links)
            "stm", // set $tm to popped value (with the aim of duplicating top of stack)
            "ltm", // load Access Link (pointer to frame of function "id" declaration)
            "ltm", // duplicate top of stack
            "push "+n.entry.offset, "add", // compute address of "id" declaration
			"lw", // load address of "id" function
            "js"  // jump to popped address (saving address of subsequent instruction in $ra)
		);
	}

	@Override
	public String visitNode(IdNode n) {
		if (print) printNode(n,n.id);

		String getAR = null;

		for (int i = 0;i<n.nl-n.entry.nl;i++) {
			getAR = nlJoin(getAR, "lw");
		}

		return nlJoin(
			"lfp", getAR, // retrieve address of frame containing "id" declaration
			              // by following the static chain (of Access Links)
			"push "+n.entry.offset, "add", // compute address of "id" declaration
			"lw" // load value of "id" variable
		);
	}

	@Override
	public String visitNode(BoolNode n) {
		if (print) printNode(n,n.val.toString());
		return "push "+(n.val?1:0);
	}

	@Override
	public String visitNode(IntNode n) {
		if (print) printNode(n,n.val.toString());
		return "push "+n.val;
	}

	@Override
	public String visitNode(ClassNode n) {
		if (print) printNode(n, n.id);
		return nlJoin(

		); // TODO implement this
	}

	@Override
	public String visitNode(NewNode n) {
		if (print) printNode(n, n.classID);

		String argCode = null;

		// Visito tutti gli argomenti (ognuno pusha il suo valore sullo stack)
		for (int i=0; i<n.arglist.size(); i++) {
			argCode = nlJoin(argCode, visit(n.arglist.get(i)));
		}

		// Per ogni argomento, carico il suo valore sull'heap, incrementando hp
		for (int i=0; i<n.arglist.size(); i++) {
			argCode = nlJoin(argCode,
					"lhp",
					"stm",
					"lhp",
					"sw",
					"ltm",
					"push 1",
					"add",
					"shp"
			);
		}

		// TODO is this finished?
		return nlJoin(
				"/* new " + n.classID + " */",
				argCode,

				// calcolo dispatch pointer
				"push " + MEMSIZE,
				"push " + n.entry.offset,
				"add",

				// carico dispatch pointer sull'heap
				"lhp",
				"sw",

				// carico sullo stack object pointer
				"lhp",

				// incremento hp
				"lhp",
				"push 1",
				"shp"
		);
	}

	@Override
	public String visitNode(ClassCallNode n) {
		if (print) printNode(n, n.methodID);
		return nlJoin(

		); // TODO implement this
	}

	@Override
	public String visitNode(EmptyNode n) {
	  if (print) printNode(n);
	  return "push -1";
	}
}