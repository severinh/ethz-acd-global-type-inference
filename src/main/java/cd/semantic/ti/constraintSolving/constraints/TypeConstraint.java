package cd.semantic.ti.constraintSolving.constraints;

import java.util.List;
import java.util.Set;

import cd.ir.symbols.TypeSymbol;
import cd.semantic.ti.constraintSolving.TypeVariable;

public abstract class TypeConstraint {
	private final List<ConstraintCondition> conditions;

	public List<ConstraintCondition> getConditions() {
		return conditions;
	}

	public TypeConstraint(List<ConstraintCondition> conditions) {
		this.conditions = conditions;
	}
	
	public boolean isActive() {
		boolean active = true;
		for (ConstraintCondition cond : conditions) {
			TypeVariable var = cond.getTypeVariable();
			Set<TypeSymbol> availableTypes = var.getTypes();
			active &= availableTypes.contains(cond.getTypeAtom());
		}
		return active;
	}
}