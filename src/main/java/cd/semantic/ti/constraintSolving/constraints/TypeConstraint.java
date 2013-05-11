package cd.semantic.ti.constraintSolving.constraints;

import java.util.List;

public abstract class TypeConstraint {
	private final List<ConstraintCondition> conditions;

	public List<ConstraintCondition> getConditions() {
		return conditions;
	}

	public TypeConstraint(List<ConstraintCondition> conditions) {
		this.conditions = conditions;
	}
}