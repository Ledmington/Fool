package compiler;

import compiler.lib.*;
import compiler.AST.*;

import java.util.*;

/*
	Classe con metodi statici di utility per definire relazioni tra tipi.
*/
public class TypeRels {

	// Map da classID a superclassID
	public static Map<String, String> superType = new HashMap<>();

	// controlla che a sia sottotipo di b
	public static boolean isSubtype(TypeNode a, TypeNode b) {
		if((a instanceof BoolTypeNode) && (b instanceof IntTypeNode) ||
				(a instanceof EmptyTypeNode) && (b instanceof RefTypeNode)) {
			return true;
		}

		// class subtyping
		if(a instanceof RefTypeNode && b instanceof RefTypeNode) {
			String current = ((RefTypeNode)a).classID;
			String end = ((RefTypeNode)b).classID;
			while(!current.equals(end)) {
				current = superType.get(current);
				if(current == null) return false;
			}
			return true;
		}

		if(a instanceof ArrowTypeNode atnA && b instanceof ArrowTypeNode atnB) {

			// covarianza tipo di ritorno (il tipo di ritorno nuovo deve essere uguale o sottotipo di quello vecchio)
			if(!isSubtype(atnA.returnType, atnB.returnType)) return false;

			// il metodo che effettua override deve avere lo stesso numero di parametri
			if(atnA.parlist.size() != atnB.parlist.size()) return false;

			// controvarianza tipi dei parametri (il tipo dell'i-esimo parametro nuovo deve essere "uguale" o supertipo dell'i-esimo parametro vecchio)
			for(int i=0; i<atnA.parlist.size(); i++) {
				TypeNode newPar = atnA.parlist.get(i);
				TypeNode oldPar = atnB.parlist.get(i);
				if(!isSubtype(oldPar, newPar)) return false;
			}

			return true;
		}

		return a.getClass().equals(b.getClass());
	}
}