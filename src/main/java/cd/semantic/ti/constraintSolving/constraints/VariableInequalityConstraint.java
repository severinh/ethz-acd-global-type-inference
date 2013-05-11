package cd.semantic.ti.constraintSolving.constraints;

import java.util.List;

import cd.semantic.ti.constraintSolving.TypeVariable;

/**
 * An inequality constriant between type variables v1 and v2, i.e. v1
 * \subseteq v2
 */
public class VariableInequalityConstraint extends TypeConstraint {
	private final TypeVariable left, right;

	public VariableInequalityConstraint(TypeVariable left,
			TypeVariable right, List<ConstraintCondition> constConditions) {
		super(constConditions);
		this.left = left;
		this.right = right;
	}

	public TypeVariable getLeft() {
		return left;
	}

	public TypeVariable getRight() {
		return right;
	}
}