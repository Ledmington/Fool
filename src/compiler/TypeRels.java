package compiler;

import compiler.lib.*;
import compiler.AST.*;

/*
	Classe con metodi statici di utility per definire relazioni tra tipi.
*/
public class TypeRels {

	public static boolean isSubtype(TypeNode a, TypeNode b) {
		return a.getClass().equals(b.getClass()) ||
				(a instanceof BoolTypeNode) && (b instanceof IntTypeNode) ||
				(a instanceof EmptyTypeNode) && (b instanceof RefTypeNode);
	}
}