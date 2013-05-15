package cd.semantic.ti.constraintSolving.constraints;

import javax.annotation.Nullable;

import cd.semantic.ti.constraintSolving.TypeVariable;

import com.google.common.collect.ImmutableList;

/**
 * An inequality constraint between type variables v1 and v2, i.e. v1 \subseteq
 * v2.
 */
public class VariableInequalityConstraint extends TypeConstraint {

	private final TypeVariable left, right;

	public VariableInequalityConstraint(TypeVariable left, TypeVariable right,
			ImmutableList<ConstraintCondition> constConditions) {
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

	@Override
	public boolean isSatisfied() {
		return !isActive() || left.isSubsetOf(right);
	}

	@Override
	public <R, A> R accept(TypeConstraintVisitor<R, A> visitor, @Nullable A arg) {
		return visitor.visit(this, arg);
	}

	@Override
	public String toString() {
		return buildString(left + "\u2286" + right);
	}

}