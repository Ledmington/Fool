package compiler;

import java.util.*;
import compiler.lib.*;

public class AST {
	
	public static class ProgLetInNode extends Node {
		final List<DecNode> declist;
		final Node exp;
		ProgLetInNode(List<DecNode> d, Node e) {
			declist = Collections.unmodifiableList(d);
			exp = e;
		}

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}

	public static class ProgNode extends Node {
		final Node exp;
		ProgNode(Node e) {exp = e;}

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}

	// Function declaration
	public static class FunNode extends DecNode {
		final String id;
		final TypeNode retType;
		final List<ParNode> parlist;
		final List<DecNode> declist; 
		final Node exp;

		FunNode(String i, TypeNode rt, List<ParNode> pl, List<DecNode> dl, Node e) {
	    	id=i; 
	    	retType=rt; 
	    	parlist=Collections.unmodifiableList(pl); 
	    	declist=Collections.unmodifiableList(dl); 
	    	exp=e;
	    }
		
		void setType(TypeNode t) {type = t;}
		
		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}

	public static class ParNode extends DecNode {
		final String id;
		ParNode(String i, TypeNode t) {id = i; type = t;}

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}
	
	public static class VarNode extends DecNode {
		final String id;
		final Node exp;
		VarNode(String i, TypeNode t, Node v) {id = i; type = t; exp = v;}

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}
		
	public static class PrintNode extends Node {
		final Node exp;
		PrintNode(Node e) {exp = e;}

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}
	
	public static class IfNode extends Node {
		final Node cond;
		final Node th;
		final Node el;
		IfNode(Node c, Node t, Node e) {cond = c; th = t; el = e;}

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}
	
	public static class EqualNode extends Node {
		final Node left;
		final Node right;
		EqualNode(Node l, Node r) {left = l; right = r;}

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}

	public static class GreaterEqualNode extends Node {
		final Node left;
		final Node right;
		GreaterEqualNode(Node l, Node r) {left = l; right = r;}

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}

	public static class LessEqualNode extends Node {
		final Node left;
		final Node right;
		LessEqualNode(Node l, Node r) {left = l; right = r;}

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}

	public static class AndNode extends Node {
		final Node left;
		final Node right;
		AndNode(Node l, Node r) {left = l; right = r;}

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}

	public static class OrNode extends Node {
		final Node left;
		final Node right;
		OrNode(Node l, Node r) {left = l; right = r;}

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}

	public static class NotNode extends Node {
		final Node exp;
		NotNode(Node e) {exp = e;}

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}
	
	public static class TimesNode extends Node {
		final Node left;
		final Node right;
		TimesNode(Node l, Node r) {left = l; right = r;}

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}

	public static class DivNode extends Node {
		final Node left;
		final Node right;
		DivNode(Node l, Node r) {left = l; right = r;}

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}
	
	public static class PlusNode extends Node {
		final Node left;
		final Node right;
		PlusNode(Node l, Node r) {left = l; right = r;}

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}

	public static class MinusNode extends Node {
		final Node left;
		final Node right;
		MinusNode(Node l, Node r) {left = l; right = r;}

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}
	
	public static class CallNode extends Node {
		final String id;
		final List<Node> arglist;
		STentry entry;
		int nl;
		CallNode(String i, List<Node> p) {
			id = i; 
			arglist = Collections.unmodifiableList(p);
		}

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}
	
	public static class IdNode extends Node {
		final String id;
		STentry entry;
		int nl;
		IdNode(String i) {id = i;}

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}
	
	public static class BoolNode extends Node {
		final Boolean val;
		BoolNode(boolean n) {val = n;}

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}
	
	public static class IntNode extends Node {
		final Integer val;
		IntNode(Integer n) {val = n;}

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}

	// dichiarazione di una classe
	public static class ClassNode extends DecNode {
		final String id;
		final String superID;  // ID classe padre
		final List<FieldNode> fields;
		final List<MethodNode> methods;
		ClassTypeNode type;
		STentry superEntry;

		public ClassNode(final String i, final String fatherID, final List<FieldNode> f, final List<MethodNode> m) {
			id = i;
			superID = fatherID;
			fields = Collections.unmodifiableList(f);
			methods = Collections.unmodifiableList(m);
		}

		@Override
		public <S, E extends Exception> S accept(final BaseASTVisitor<S, E> visitor) throws E {
			return visitor.visitNode(this);
		}
	}

	// dichiarazione di un campo
	public static class FieldNode extends ParNode {
		public int offset;
		FieldNode(String i, TypeNode t) {
			super(i, t);
		}

		@Override
		public <S, E extends Exception> S accept(final BaseASTVisitor<S, E> visitor) throws E {
			return visitor.visitNode(this);
		}
	}

	// dichiarazione di un metodo (l'invocazione dall'interno è CallNode)
	public static class MethodNode extends FunNode {
		public String label;
		public int offset;

		MethodNode(final String i, final TypeNode rt, final List<ParNode> pl, final List<DecNode> dl, final Node e) {
			super(i, rt, pl, dl, e);
		}

		@Override
		public <S, E extends Exception> S accept(final BaseASTVisitor<S, E> visitor) throws E {
			return visitor.visitNode(this);
		}
	}

	// invocazione metodo dall'esterno
	public static class ClassCallNode extends Node {
		final String objID;
		final String methodID;
		final List<Node> arglist;
		STentry entry; // entry dell'oggetto
		STentry methodEntry; // entry del metodo
		int nl;

		public ClassCallNode(final String oID, final String mID, final List<Node> args) {
			objID = oID;
			methodID = mID;
			arglist = Collections.unmodifiableList(args);
		}

		@Override
		public <S, E extends Exception> S accept(final BaseASTVisitor<S, E> visitor) throws E {
			return visitor.visitNode(this);
		}
	}

	// istanziazione di un oggetto
	public static class NewNode extends Node {
		final String classID;
		final List<Node> arglist;
		STentry entry;

		public NewNode(final String cID, final List<Node> args) {
			classID = cID;
			arglist = Collections.unmodifiableList(args);
		}

		@Override
		public <S, E extends Exception> S accept(final BaseASTVisitor<S, E> visitor) throws E {
			return visitor.visitNode(this);
		}
	}

	// null
	public static class EmptyNode extends Node {
		@Override
		public <S, E extends Exception> S accept(final BaseASTVisitor<S, E> visitor) throws E {
			return visitor.visitNode(this);
		}
	}
	
	public static class ArrowTypeNode extends TypeNode {
		final List<TypeNode> parlist;
		final TypeNode returnType;

		ArrowTypeNode(List<TypeNode> p, TypeNode r) {
			parlist = Collections.unmodifiableList(p); 
			returnType = r;
		}

		@Override
		public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {return visitor.visitNode(this);}

		public String toString() {
			return parlist.toString() + " -> " + returnType;
		}
	}
	
	public static class BoolTypeNode extends TypeNode {
		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}

		public String toString() {
			return "bool";
		}
	}

	public static class IntTypeNode extends TypeNode {
		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}

		public String toString() {
			return "int";
		}
	}

	// tipo di una classe
	public static class ClassTypeNode extends TypeNode {
		final List<TypeNode> allFields;
		final List<ArrowTypeNode> allMethods;

		public ClassTypeNode(List<TypeNode> f, List<ArrowTypeNode> m) {
			allFields = f;
			allMethods = m;
		}

		@Override
		public <S, E extends Exception> S accept(final BaseASTVisitor<S, E> visitor) throws E {
			return visitor.visitNode(this);
		}
	}

	// tipo di un metodo (per distinguerlo dalle funzioni)
	public static class MethodTypeNode extends TypeNode {
		final ArrowTypeNode fun;

		public MethodTypeNode(final ArrowTypeNode f) {
			fun = f;
		}

		@Override
		public <S, E extends Exception> S accept(final BaseASTVisitor<S, E> visitor) throws E {
			return visitor.visitNode(this);
		}

		public String toString() {
			return fun.toString();
		}
	}

	// riferimento ad una classe (tipo quando si invoca un metodo dall'esterno)
	public static class RefTypeNode extends TypeNode {
		final String classID;

		public RefTypeNode(String classID) {
			this.classID = classID;
		}

		@Override
		public <S, E extends Exception> S accept(final BaseASTVisitor<S, E> visitor) throws E {
			return visitor.visitNode(this);
		}

		public String toString() {
			return classID;
		}
	}

	// tipo di "null"
	public static class EmptyTypeNode extends TypeNode {
		@Override
		public <S, E extends Exception> S accept(final BaseASTVisitor<S, E> visitor) throws E {
			return visitor.visitNode(this);
		}
	}

}