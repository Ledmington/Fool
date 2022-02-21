package compiler;

import compiler.exc.*;
import compiler.lib.*;
import compiler.AST.*;

import static compiler.TypeRels.*;

//visitNode(n) fa il type checking di un Node n e ritorna:
//- per una espressione, il suo tipo (oggetto BoolTypeNode o IntTypeNode)
//- per una dichiarazione, "null"; controlla la correttezza interna della dichiarazione
//(- per un tipo: "null"; controlla che il tipo non sia incompleto) 
//
//visitSTentry(s) ritorna, per una STentry s, il tipo contenuto al suo interno
public class TypeCheckEASTVisitor extends BaseEASTVisitor<TypeNode, TypeException> {

	public TypeCheckEASTVisitor() { super(true); } // enables incomplete tree exceptions
	public TypeCheckEASTVisitor(boolean debug) { super(true, debug); } // enables print for debugging

	//checks that a type object is visitable (not incomplete) 
	private TypeNode ckvisit(TypeNode t) throws TypeException {
		visit(t);
		return t;
	} 
	
	@Override
	public TypeNode visitNode(ProgLetInNode n) throws TypeException {
		if (print) printNode(n);

		for (Node dec : n.declist) {
			try {
				visit(dec);
			} catch (IncomplException ignored) {
			} catch (TypeException e) {
				System.out.println("Type checking error in a declaration: " + e.text);
			}
		}

		return visit(n.exp);
	}

	@Override
	public TypeNode visitNode(ProgNode n) throws TypeException {
		if (print) printNode(n);
		return visit(n.exp);
	}

	@Override
	public TypeNode visitNode(FunNode n) throws TypeException {
		if (print) printNode(n, n.id);

		for (Node dec : n.declist) {
			try {
				visit(dec);
			} catch (IncomplException ignored) {
			} catch (TypeException e) {
				System.out.println("Type checking error in a declaration: " + e.text);
			}
		}

		if ( !isSubtype(visit(n.exp), ckvisit(n.retType)) )
			throw new TypeException("Wrong return type for function " + n.id, n.getLine());

		return null;
	}

	@Override
	public TypeNode visitNode(VarNode n) throws TypeException {
		if (print) printNode(n, n.id);
		if ( !isSubtype(visit(n.exp), ckvisit(n.getType())) )
			throw new TypeException("Incompatible value for variable " + n.id, n.getLine());
		return null;
	}

	@Override
	public TypeNode visitNode(PrintNode n) throws TypeException {
		if (print) printNode(n);
		return visit(n.exp);
	}

	@Override
	public TypeNode visitNode(IfNode n) throws TypeException {
		if (print) printNode(n);
		if ( !(isSubtype(visit(n.cond), new BoolTypeNode())) )
			throw new TypeException("Non boolean condition in if", n.getLine());
		TypeNode t = visit(n.th);
		TypeNode e = visit(n.el);
		if (isSubtype(t, e)) return e;
		if (isSubtype(e, t)) return t;
		TypeNode ancestor = lowestCommonAncestor(t, e);
		if(ancestor != null) return ancestor;
		throw new TypeException("Incompatible types in then-else branches", n.getLine());
	}

	@Override
	public TypeNode visitNode(EqualNode n) throws TypeException {
		if (print) printNode(n);
		TypeNode l = visit(n.left);
		TypeNode r = visit(n.right);
		if ( !(isSubtype(l, r) || isSubtype(r, l)) )
			throw new TypeException("Incompatible types in equal", n.getLine());
		return new BoolTypeNode();
	}

	@Override
	public TypeNode visitNode(GreaterEqualNode n) throws TypeException {
		if (print) printNode(n);
		TypeNode l = visit(n.left);
		TypeNode r = visit(n.right);
		if ( !(isSubtype(l, r) || isSubtype(r, l)) )
			throw new TypeException("Incompatible types in greater-equal", n.getLine());
		return new BoolTypeNode();
	}

	@Override
	public TypeNode visitNode(LessEqualNode n) throws TypeException {
		if (print) printNode(n);
		TypeNode l = visit(n.left);
		TypeNode r = visit(n.right);
		if ( !(isSubtype(l, r) || isSubtype(r, l)) )
			throw new TypeException("Incompatible types in less-equal", n.getLine());
		return new BoolTypeNode();
	}

	@Override
	public TypeNode visitNode(OrNode n) throws TypeException {
		if (print) printNode(n);
		TypeNode l = visit(n.left);
		TypeNode r = visit(n.right);
		if ( !(l instanceof BoolTypeNode) || !(r instanceof BoolTypeNode) ) {
			throw new TypeException("Non-boolean in or", n.getLine());
		}
		return new BoolTypeNode();
	}

	@Override
	public TypeNode visitNode(AndNode n) throws TypeException {
		if (print) printNode(n);
		TypeNode l = visit(n.left);
		TypeNode r = visit(n.right);
		if ( !(l instanceof BoolTypeNode) || !(r instanceof BoolTypeNode) ) {
			throw new TypeException("Non-boolean in and", n.getLine());
		}
		return new BoolTypeNode();
	}

	@Override
	public TypeNode visitNode(NotNode n) throws TypeException {
		if (print) printNode(n);
		TypeNode t = visit(n.exp);
		if ( !(t instanceof BoolTypeNode) ) {
			throw new TypeException("Non-boolean in not", n.getLine());
		}
		return new BoolTypeNode();
	}

	@Override
	public TypeNode visitNode(TimesNode n) throws TypeException {
		if (print) printNode(n);
		if ( !(isSubtype(visit(n.left), new IntTypeNode())
				&& isSubtype(visit(n.right), new IntTypeNode())) )
			throw new TypeException("Non integers in multiplication", n.getLine());
		return new IntTypeNode();
	}

	@Override
	public TypeNode visitNode(DivNode n) throws TypeException {
		if (print) printNode(n);
		if ( !(isSubtype(visit(n.left), new IntTypeNode())
				&& isSubtype(visit(n.right), new IntTypeNode())) )
			throw new TypeException("Non integers in division", n.getLine());
		return new IntTypeNode();
	}

	@Override
	public TypeNode visitNode(PlusNode n) throws TypeException {
		if (print) printNode(n);
		if ( !(isSubtype(visit(n.left), new IntTypeNode())
				&& isSubtype(visit(n.right), new IntTypeNode())) )
			throw new TypeException("Non integers in sum", n.getLine());
		return new IntTypeNode();
	}

	@Override
	public TypeNode visitNode(MinusNode n) throws TypeException {
		if (print) printNode(n);
		if ( !(isSubtype(visit(n.left), new IntTypeNode())
				&& isSubtype(visit(n.right), new IntTypeNode())) )
			throw new TypeException("Non integers in subtraction", n.getLine());
		return new IntTypeNode();
	}

	@Override
	public TypeNode visitNode(CallNode n) throws TypeException {
		if (print) printNode(n, n.id);

		TypeNode t = visit(n.entry);
		if(t instanceof MethodTypeNode) {
			t = ((MethodTypeNode) t).fun;
		}

		if ( !(t instanceof ArrowTypeNode at) )
			throw new TypeException("Invocation of a non-function "+n.id, n.getLine());

		if ( !(at.parlist.size() == n.arglist.size()) )
			throw new TypeException("Wrong number of parameters in the invocation of "+n.id, n.getLine());

		for (int i = 0; i < n.arglist.size(); i++)
			if ( !(isSubtype(visit(n.arglist.get(i)), at.parlist.get(i))) )
				throw new TypeException("Wrong type for "+i+"-th parameter in the invocation of "+n.id, n.getLine());

		return at.returnType;
	}

	@Override
	public TypeNode visitNode(IdNode n) throws TypeException {
		if (print) printNode(n, n.id);
		TypeNode t = visit(n.entry); 
		if (t instanceof ArrowTypeNode)
			throw new TypeException("Wrong usage of function identifier " + n.id, n.getLine());
		return t;
	}

	@Override
	public TypeNode visitNode(BoolNode n) {
		if (print) printNode(n,n.val.toString());
		return new BoolTypeNode();
	}

	@Override
	public TypeNode visitNode(IntNode n) {
		if (print) printNode(n,n.val.toString());
		return new IntTypeNode();
	}

// gestione tipi incompleti	(se lo sono lancia eccezione)
	
	@Override
	public TypeNode visitNode(ArrowTypeNode n) throws TypeException {
		if (print) printNode(n);
		for (Node par: n.parlist) visit(par);
		visit(n.returnType, "->"); //marks return type
		return null;
	}

	@Override
	public TypeNode visitNode(BoolTypeNode n) {
		if (print) printNode(n);
		return null;
	}

	@Override
	public TypeNode visitNode(IntTypeNode n) {
		if (print) printNode(n);
		return null;
	}

	@Override
	public TypeNode visitNode(RefTypeNode n) {
		if (print) printNode(n);
		return null;
	}

	@Override
	public TypeNode visitNode(MethodTypeNode n) {
		if (print) printNode(n);
		return null;
	}

	@Override
	public TypeNode visitNode(NewNode n) throws TypeException {
		if (print) printNode(n);

		// Handle incomplete nodes
		if (n.entry == null) {
			throw new TypeException("Invalid type", n.getLine());
		}

		ClassTypeNode ctn = ((ClassTypeNode) n.entry.type);

		if ( n.arglist.size() != ctn.allFields.size() ) {
			throw new TypeException("Wrong number of parameters for new " + n.classID, n.getLine());
		}

		for (int i=0; i<ctn.allFields.size(); i++) {
			Node arg = n.arglist.get(i);
			TypeNode t = visit(arg);

			if (!(isSubtype(t, ctn.allFields.get(i)))) {
				throw new TypeException("Wrong type of "+i+"-th parameter", n.getLine());
			}
		}

		return new RefTypeNode(n.classID);
	}

	@Override
	public TypeNode visitNode(ClassNode n) throws TypeException {
		if (print) printNode(n, n.id + ((n.superID==null)?"":" extends "+n.superID));

		if(n.superID != null) {
			superType.put(n.id, n.superID);

			ClassTypeNode parentCT = (ClassTypeNode) n.superEntry.type;

			// confronto che gli eventuali overriding di campi siano corretti
			for(int f=0; f<n.fields.size(); f++) {
				int position = -n.fields.get(f).offset - 1;

				if(position < parentCT.allFields.size() && f < n.type.allFields.size()) {
					// overriding
					if( !isSubtype(n.type.allFields.get(position), parentCT.allFields.get(position)) ) {
						throw new TypeException("Invalid overriding of " + f + "-th field in class " + n.id, n.getLine());
					}
				}
			}

			// confronto che gli eventuali overriding di metodi siano corretti
			for(int m=0; m<n.methods.size(); m++) {
				int position = n.methods.get(m).offset;

				if(position < parentCT.allMethods.size()) {
					// overriding
					ArrowTypeNode atn = n.type.allMethods.get(position);
					ArrowTypeNode fatherAtn = parentCT.allMethods.get(position);
					if( !isSubtype(atn, fatherAtn) ) {
						throw new TypeException("Invalid overriding of " + m + "-th method in class " + n.id, n.getLine());
					}
				} else {
					// senza overriding visito il metodo
					visit(n.methods.get(m));
				}
			}
		} else {
			// se non eredito, visito tutti i metodi
			for (int m=0; m < n.methods.size(); m++) {
				visit(n.methods.get(m));
			}
		}

		return null;
	}

	@Override
	public TypeNode visitNode(ClassCallNode n) throws TypeException {
		if (print) printNode(n, n.objID+"."+n.methodID);

		if(n.methodEntry == null) return null; // early exit to avoid NullPointerException on wrong ClassCallNodes

		if(n.methodEntry.type instanceof MethodTypeNode) {
			ArrowTypeNode atn = ((MethodTypeNode) n.methodEntry.type).fun;

			if(n.arglist.size() != atn.parlist.size()) {
				throw new TypeException("Wrong number of arguments in method " + n.methodID + " invocation", n.getLine());
			}

			for(int i=0; i<n.arglist.size(); i++) {
				TypeNode argType = visit(n.arglist.get(i));
				TypeNode declarationArgType = atn.parlist.get(i);
				if(!isSubtype(argType, declarationArgType)) {
					throw new TypeException("Wrong type of " + i + "-th argument in method " + n.methodID + " invocation", n.getLine());
				}
			}

			return atn.returnType;
		}

		return null;
	}

	@Override
	public TypeNode visitNode(MethodNode n) throws TypeException {
		if (print) printNode(n, n.id);

		for (Node dec : n.declist) {
			try {
				visit(dec);
			} catch (IncomplException ignored) {
			} catch (TypeException e) {
				System.out.println("Type checking error in a declaration: " + e.text);
			}
		}

		if ( !isSubtype(visit(n.exp), ckvisit(n.retType)) ) {
			throw new TypeException("Wrong return type for method " + n.id, n.getLine());
		}

		return null;
	}

	@Override
	public TypeNode visitNode(ClassTypeNode n) {
		if (print) printNode(n);
		return null;
	}

	@Override
	public TypeNode visitNode(EmptyNode n) {
		if (print) printNode(n);
		return new EmptyTypeNode();
	}

	@Override
	public TypeNode visitNode(EmptyTypeNode n) {
		if (print) printNode(n);
		return null;
	}

	// STentry (ritorna campo type)

	@Override
	public TypeNode visitSTentry(STentry entry) throws TypeException {
		if (print) printSTentry("type");
		return ckvisit(entry.type); 
	}
}