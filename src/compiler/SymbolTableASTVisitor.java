package compiler;

import compiler.exc.*;
import compiler.lib.*;
import compiler.AST.*;

import java.util.*;

public class SymbolTableASTVisitor extends BaseASTVisitor<Void, VoidException> {
	
	private final List<Map<String, STentry>> symTable = new ArrayList<>();
	private int nestingLevel = 0; // current nesting level
	private int decOffset = -2; // counter for offset of local declarations at current nesting level
	public int stErrors = 0;

	private final Map<String, // id della classe
			Map<String, STentry> // virtual table della classe
			> classTable = new HashMap<>();

	private Set<String> symbolIDs;

	public SymbolTableASTVisitor() {}
	public SymbolTableASTVisitor(boolean debug) {super(debug);} // enables print for debugging

	private STentry stLookup(String id) {
		if(symTable.isEmpty()) return null;

		int j = nestingLevel;
		STentry entry = null;
		while (j >= 0 && entry == null) 
			entry = symTable.get(j--).get(id);	
		return entry;
	}

	// virtual table lookup
	private STentry vtLookup(String classID, String id) {
		STentry entry = null;
		if (classTable.containsKey(classID)) {
			Map<String, STentry> virtualTable = classTable.get(classID);
			if(virtualTable.containsKey(id)) {
				entry = virtualTable.get(id);
			}
		}
		return entry;
	}

	@Override
	public Void visitNode(ProgLetInNode n) {
		if (print) printNode(n);

		Map<String, STentry> hm = new HashMap<>();
		symTable.add(hm);
	    for (Node dec : n.declist) visit(dec);

		visit(n.exp);
		symTable.remove(0);

		return null;
	}

	@Override
	public Void visitNode(ProgNode n) {
		if (print) printNode(n);
		visit(n.exp);
		return null;
	}
	
	@Override
	public Void visitNode(FunNode n) {
		if (print) printNode(n);

		Map<String, STentry> hm = symTable.get(nestingLevel);
		List<TypeNode> parTypes = new ArrayList<>();  
		for (ParNode par : n.parlist) parTypes.add(par.getType());
		ArrowTypeNode atn = new ArrowTypeNode(parTypes, n.retType);
		n.setType(atn);
		STentry entry = new STentry(nestingLevel, atn, decOffset--);

		//inserimento di ID nella symtable
		if (hm.put(n.id, entry) != null) {
			System.out.println("Fun id " + n.id + " at line "+ n.getLine() +" already declared");
			stErrors++;
		} 

		//creare una nuova hashmap per la symTable
		nestingLevel++;
		Map<String, STentry> hmn = new HashMap<>();
		symTable.add(hmn);
		int prevNLDecOffset = decOffset; // stores counter for offset of declarations at previous nesting level
		decOffset = -2;
		
		int parOffset = 1;
		for (ParNode par : n.parlist)
			if (hmn.put(par.id, new STentry(nestingLevel, par.getType(), parOffset++)) != null) {
				System.out.println("Par id " + par.id + " at line "+ n.getLine() +" already declared");
				stErrors++;
			}
		for (Node dec : n.declist) visit(dec);
		visit(n.exp);

		//rimuovere la hashmap corrente poiche' esco dallo scope               
		symTable.remove(nestingLevel--);
		decOffset = prevNLDecOffset; // restores counter for offset of declarations at previous nesting level

		return null;
	}
	
	@Override
	public Void visitNode(VarNode n) {
		if (print) printNode(n);

		visit(n.exp);
		Map<String, STentry> hm = symTable.get(nestingLevel);
		STentry entry = new STentry(nestingLevel, n.getType(), decOffset--);

		//inserimento di ID nella symtable
		if (hm.put(n.id, entry) != null) {
			System.out.println("Var id " + n.id + " at line "+ n.getLine() + " already declared");
			stErrors++;
		}

		return null;
	}

	@Override
	public Void visitNode(PrintNode n) {
		if (print) printNode(n);
		visit(n.exp);
		return null;
	}

	@Override
	public Void visitNode(IfNode n) {
		if (print) printNode(n);
		visit(n.cond);
		visit(n.th);
		visit(n.el);
		return null;
	}
	
	@Override
	public Void visitNode(EqualNode n) {
		if (print) printNode(n);
		visit(n.left);
		visit(n.right);
		return null;
	}

	@Override
	public Void visitNode(GreaterEqualNode n) {
		if (print) printNode(n);
		visit(n.left);
		visit(n.right);
		return null;
	}

	@Override
	public Void visitNode(LessEqualNode n) {
		if (print) printNode(n);
		visit(n.left);
		visit(n.right);
		return null;
	}

	@Override
	public Void visitNode(OrNode n) {
		if (print) printNode(n);
		visit(n.left);
		visit(n.right);
		return null;
	}

	@Override
	public Void visitNode(AndNode n) {
		if (print) printNode(n);
		visit(n.left);
		visit(n.right);
		return null;
	}

	@Override
	public Void visitNode(NotNode n) {
		if (print) printNode(n);
		visit(n.exp);
		return null;
	}
	
	@Override
	public Void visitNode(TimesNode n) {
		if (print) printNode(n);
		visit(n.left);
		visit(n.right);
		return null;
	}

	@Override
	public Void visitNode(DivNode n) {
		if (print) printNode(n);
		visit(n.left);
		visit(n.right);
		return null;
	}
	
	@Override
	public Void visitNode(PlusNode n) {
		if (print) printNode(n);
		visit(n.left);
		visit(n.right);
		return null;
	}

	@Override
	public Void visitNode(MinusNode n) {
		if (print) printNode(n);
		visit(n.left);
		visit(n.right);
		return null;
	}

	@Override
	public Void visitNode(CallNode n) {
		if (print) printNode(n);

		// cerca id della funzione nella symbol table
		STentry entry = stLookup(n.id);
		if (entry == null) {
			System.out.println("Fun id " + n.id + " at line "+ n.getLine() + " not declared");
			stErrors++;
		} else {
			n.entry = entry;
			n.nl = nestingLevel;
		}

		for (Node arg : n.arglist) visit(arg);

		return null;
	}

	@Override
	public Void visitNode(ClassNode n) {
		if (print) printNode(n);

		// Preparing the set for not-inherited declarations
		symbolIDs = new HashSet<>();

		ClassTypeNode classTypeNode;
		ClassTypeNode superType = null;
		Map<String, STentry> vt; // virtual table

		// se non si eredita, la lista dei campi, la lista dei metodi e la virtual table sono vuote
		if(n.superID == null) {
			 classTypeNode = new ClassTypeNode(
					new ArrayList<>(), // fields
					new ArrayList<>()  // methods
			);
			 vt = new HashMap<>();
		} else {
			// se si eredita, controllo che la superclasse esista
			if(!classTable.containsKey(n.superID)) {
				System.out.println("Superclass id " + n.id + " at line " + n.getLine() + " not declared");
				stErrors++;
			}

			// estraggo il tipo della sueprclasse
			n.superEntry = symTable.get(0).get(n.superID);
			superType = (ClassTypeNode) n.superEntry.type;

			// inizializzo il classType e la virtual table con quelle delle superclasse
			classTypeNode = new ClassTypeNode(
					new ArrayList<>(superType.allFields),
					new ArrayList<>(superType.allMethods)
			);
			vt = new HashMap<>(classTable.get(n.superID));
		}

		// Creating the STentry for the current class (and setting its type)
		STentry classEntry = new STentry(0, classTypeNode, decOffset--);
		n.type = classTypeNode;

		// Checking that the class is not already declared
		if (symTable.get(0).put(n.id, classEntry) != null) {
			System.out.println("Class id " + n.id + " at line " + n.getLine() + " already declared");
			stErrors++;
		}

		// Adding class's virtual table
		classTable.put(n.id, vt);
		symTable.add(vt);

		// Preparing the offset for field declarations
		int prevDecOffset = decOffset;
		decOffset = (n.superID == null) ? -1 : -superType.allFields.size() - 1;
		nestingLevel++;

		// adding fields without visit
		for(int i=0; i<n.fields.size(); i++) {
			FieldNode field = n.fields.get(i);

			// adding field to virtual table
			if(!symbolIDs.add(field.id)) { // if a field is redeclared
				System.out.println("Field id " + field.id + " at line " + field.getLine() + " already declared");
				stErrors++;
			} else {
				if(!vt.containsKey(field.id)) {
					// not-overriding fields
					vt.put(field.id, new STentry(nestingLevel, field.getType(), decOffset));
					field.offset = decOffset;
					decOffset--;
					classTypeNode.allFields.add(field.getType());
				} else {
					// overriding
					STentry oldEntry = vt.get(field.id);

					// invalid overriding
					if(oldEntry.type instanceof MethodTypeNode) {
						System.out.println("Field " + n.id+"."+field.id + " cannot override a method in superclass");
						stErrors++;
					} else {
						// correct overriding
						vt.put(field.id, new STentry(nestingLevel, field.getType(), oldEntry.offset));
						field.offset = oldEntry.offset;
						classTypeNode.allFields.set(i, field.getType());
					}
				}
			}
		}

		// preparing offset for method declarations
		decOffset = (n.superID == null) ? 0 : superType.allMethods.size();

		// Visiting methods
		for(int i=0; i<n.methods.size(); i++) {
			MethodNode meth = n.methods.get(i);

			visit(meth);

			// Extracting the method type
			ArrowTypeNode methATN = ((MethodTypeNode) meth.getType()).fun;

			if(!vt.containsKey(meth.id)) {
				// not-overriding methods
				vt.put(meth.id, new STentry(nestingLevel, meth.getType(), decOffset));
				meth.offset = decOffset;
				decOffset++;
				classTypeNode.allMethods.add(methATN);
			} else {
				// overriding
				STentry oldEntry = vt.get(meth.id);

				// invalid overriding
				if(!(oldEntry.type instanceof MethodTypeNode)) {
					System.out.println("Field " + n.id+"."+meth.id + " cannot override a method in superclass");
					stErrors++;
				} else {
					// correct overriding
					vt.put(meth.id, new STentry(nestingLevel, meth.getType(), oldEntry.offset));
					meth.offset = oldEntry.offset;
					classTypeNode.allMethods.set(oldEntry.offset, methATN);
				}
			}
		}

		// Resetting old values of nesting level and offset
		decOffset = prevDecOffset;
		nestingLevel--;

		// Removing the class's virtual table from the symbol table after visiting the declaration
		symTable.remove(vt);

		return null;
	}

	@Override
	public Void visitNode(MethodNode n) {
		if (print) printNode(n);

		// Checking that the method is not already declared
		if(!symbolIDs.add(n.id)) {
			System.out.println("Method id " + n.id + " at line " + n.getLine() + " already declared");
			stErrors++;
		}

		// Creating and setting the method type
		List<TypeNode> parTypes = n.parlist.stream()
				.map(DecNode::getType)
				.toList(); // Collecting parameters
		MethodTypeNode methodType = new MethodTypeNode(new ArrowTypeNode(parTypes, n.retType));
		n.setType(methodType);

		// Preparing the offset for the parameters
		int prevNLOffset = decOffset;
		decOffset = 1;

		// Preparing the map for the next nesting level
		HashMap<String, STentry> nlMethodMap = new HashMap<>();
		nestingLevel++;
		symTable.add(nlMethodMap);

		// Setting parameters without visit
		for (ParNode par : n.parlist) {
			if (nlMethodMap.put(par.id, new STentry(nestingLevel, par.getType(), decOffset++)) != null) {
				System.out.println("Par id " + par.id + " at line " + par.getLine() + " already declared");
				stErrors++;
			}
		}

		// Visiting declarations
		for(DecNode dec : n.declist) {
			visit(dec);
		}

		// Visiting method body
		visit(n.exp);

		decOffset = prevNLOffset;

		nestingLevel--;
		symTable.remove(nlMethodMap);

		return null;
	}

	@Override
	public Void visitNode(ClassCallNode n) {
		if (print) printNode(n);

		// Looking for existing object in the symbol table
		STentry objEntry = stLookup(n.objID);
		if (objEntry == null) {
			System.out.println("Object " + n.objID + " at line "+ n.getLine() + " not declared");
			stErrors++;
			return null; // avoid dereferencing a null pointer
		} else {
			n.entry = objEntry;
			n.nl = nestingLevel;
		}

		String classID = ((RefTypeNode) objEntry.type).classID;

		// Looking for method in the virtual table
		STentry methodEntry = vtLookup(classID, n.methodID);
		if (methodEntry == null) {
			System.out.println("Method " + n.methodID + " at line " + n.getLine() + " not declared in class " + classID);
			stErrors++;
		} else {
			n.methodEntry = methodEntry;
			n.nl = nestingLevel;
		}

		for (Node arg : n.arglist) {
			visit(arg);
		}

		return null;
	}

	@Override
	public Void visitNode(NewNode n) {
		if (print) printNode(n);

		// checking that the class exists
		String classID = n.classID;
		if (!classTable.containsKey(classID)) {
			System.out.println("Class id " + n.classID + " at line " + n.getLine() + " not declared");
			stErrors++;
		}

		// Setting the entry as the one of the class
		n.entry = symTable.get(0).get(classID);

		for (Node arg : n.arglist) {
			visit(arg);
		}

		return null;
	}

	@Override
	public Void visitNode(IdNode n) {
		if (print) printNode(n);

		STentry entry = stLookup(n.id);
		if (entry == null) {
			System.out.println("Var or Par id " + n.id + " at line " + n.getLine() + " not declared");
			stErrors++;
		} else {
			n.entry = entry;
			n.nl = nestingLevel;
		}

		return null;
	}

	@Override
	public Void visitNode(BoolNode n) {
		if (print) printNode(n, n.val.toString());
		return null;
	}

	@Override
	public Void visitNode(IntNode n) {
		if (print) printNode(n, n.val.toString());
		return null;
	}

	@Override
	public Void visitNode(EmptyNode n) {
		if (print) printNode(n, "null");
		return null;
	}
}
