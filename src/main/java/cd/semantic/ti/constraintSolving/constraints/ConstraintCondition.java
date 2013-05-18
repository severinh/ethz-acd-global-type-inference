package cd.semantic.ti.constraintSolving.constraints;

import cd.ir.symbols.TypeSymbol;
import cd.semantic.ti.constraintSolving.TypeSet;

/**
 * Condition that constraints may depend on. It is of the form a \in v, where a
 * is a type symbol (an atom) and v is a type set (usually variable).
 */
public class ConstraintCondition {
	private final TypeSymbol typeAtom;
	private final TypeSet typeSet;

	public ConstraintCondition(TypeSymbol typeAtom, TypeSet typeSet) {
		this.typeAtom = typeAtom;
		this.typeSet = typeSet;
	}

	public TypeSymbol getTypeAtom() {
		return typeAtom;
	}

	public TypeSet getTypeSet() {
		return typeSet;
	}

	public boolean isSatisfied() {
		boolean result = getTypeSet().getTypes().contains(getTypeAtom());
		return result;
	}

	@Override
	public String toString() {
		return typeAtom + " \u2208 " + typeSet;
	}
}