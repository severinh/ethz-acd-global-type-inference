package cd.semantic.ti.constraintSolving.constraints;

import static com.google.common.base.Preconditions.checkNotNull;
import cd.ir.symbols.TypeSymbol;
import cd.semantic.ti.constraintSolving.TypeVariable;

/**
 * Condition that constraints may depend on. It is of the form a \in v, where a
 * is a type symbol (an atom) and v is a type variable.
 */
public class ConstraintCondition {
	private final TypeSymbol typeAtom;
	private final TypeVariable typeVariable;

	public ConstraintCondition(TypeSymbol typeAtom, TypeVariable typeVariable) {
		this.typeAtom = checkNotNull(typeAtom);
		this.typeVariable = checkNotNull(typeVariable);
	}

	public TypeSymbol getTypeAtom() {
		return typeAtom;
	}

	public TypeVariable getTypeVariable() {
		return typeVariable;
	}

	public boolean isSatisfied() {
		boolean result = getTypeVariable().getTypes().contains(getTypeAtom());
		return result;
	}
}