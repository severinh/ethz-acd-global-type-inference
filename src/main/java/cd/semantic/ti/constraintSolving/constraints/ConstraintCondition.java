package cd.semantic.ti.constraintSolving.constraints;

import cd.ir.symbols.TypeSymbol;
import cd.semantic.ti.constraintSolving.TypeVariable;

/**
 * Condition that constraints may depend on. It is of the form a \in v,
 * where a is a type symbol (an atom) and v is a type variable.
 */
public class ConstraintCondition {
	private final TypeSymbol typeAtom;
	private final TypeVariable typeVariable;

	public ConstraintCondition(TypeSymbol typeAtom,
			TypeVariable typeVariable) {
		this.typeAtom = typeAtom;
		this.typeVariable = typeVariable;
	}

	public TypeSymbol getTypeAtom() {
		return typeAtom;
	}

	public TypeVariable getTypeVariable() {
		return typeVariable;
	}
}