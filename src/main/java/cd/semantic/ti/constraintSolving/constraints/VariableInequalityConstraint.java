package cd.semantic.ti.constraintSolving.constraints;

import cd.semantic.ti.constraintSolving.TypeVariable;

import com.google.common.collect.ImmutableList;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An inequality constraint between type variables v1 and v2, i.e. v1 \subseteq
 * v2.
 */
public class VariableInequalityConstraint extends TypeConstraint {
	private final TypeVariable left, right;

	public VariableInequalityConstraint(TypeVariable left, TypeVariable right,
			ImmutableList<ConstraintCondition> constConditions) {
		super(constConditions);
		this.left = checkNotNull(left);
		this.right = checkNotNull(right);
	}

	public TypeVariable getLeft() {
		return left;
	}

	public TypeVariable getRight() {
		return right;
	}

	@Override
	public boolean isSatisfied() {
		return !isActive() || left.isSubsetOf(right);
	}

	@Override
	public <R, A> R accept(TypeConstraintVisitor<R, A> visitor, A arg) {
		return visitor.visitVariableInequalityConstraint(this, arg);
	}

	@Override
	public String toString() {
		return buildString(left + "\u2286" + right);
	}
}