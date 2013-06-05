package cd.semantic.ti.palsberg.constraints;

import javax.annotation.Nullable;

import cd.semantic.ti.palsberg.solving.TypeVariable;

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

	@Override
	public TypeVariable getSubTypeSet() {
		return left;
	}

	@Override
	public TypeVariable getSuperTypeSet() {
		return right;
	}

	@Override
	public <R, A> R accept(TypeConstraintVisitor<R, A> visitor, @Nullable A arg) {
		return visitor.visit(this, arg);
	}

}