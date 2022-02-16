package compiler;

import compiler.lib.*;
import compiler.AST.*;

import java.util.*;

/*
	Classe con metodi statici di utility per definire relazioni tra tipi.
*/
public class TypeRels {

	public static Map<String, String> superType = new HashMap<>();

	public static boolean isSubtype(TypeNode a, TypeNode b) {
		if(a.getClass().equals(b.getClass()) ||
				(a instanceof BoolTypeNode) && (b instanceof IntTypeNode) ||
				(a instanceof EmptyTypeNode) && (b instanceof RefTypeNode)) {
			return true;
		}

		if(a instanceof RefTypeNode && b instanceof RefTypeNode) {
			String current = ((RefTypeNode)a).classID;
			String end = ((RefTypeNode)b).classID;
			while(!current.equals(end)) {
				current = superType.get(current);
				if(current == null) return false;
			}
			return true;
		} else return false;
	}
}