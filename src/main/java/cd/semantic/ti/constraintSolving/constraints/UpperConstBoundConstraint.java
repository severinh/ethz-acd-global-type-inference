package cd.semantic.ti.constraintSolving.constraints;

import com.google.common.collect.ImmutableList;

import cd.semantic.ti.constraintSolving.ConstantTypeSet;
import cd.semantic.ti.constraintSolving.TypeVariable;

/**
 * An upper bound constraint for a type variable v, i.e. v \subseteq U, where U
 * is the upper bound (a constant set).
 */
public class UpperConstBoundConstraint extends TypeConstraint {

	private final TypeVariable typeVariable;
	private final ConstantTypeSet upperBound;

	public UpperConstBoundConstraint(TypeVariable typeVariable,
			ConstantTypeSet upperBound,
			ImmutableList<ConstraintCondition> conditions) {
		super(conditions);
		this.typeVariable = typeVariable;
		this.upperBound = upperBound;
	}

	public TypeVariable getTypeVariable() {
		return typeVariable;
	}

	public ConstantTypeSet getUpperBound() {
		return upperBound;
	}

	@Override
	public TypeVariable getSubTypeSet() {
		return typeVariable;
	}

	@Override
	public ConstantTypeSet getSuperTypeSet() {
		return upperBound;
	}

	@Override
	public <R, A> R accept(TypeConstraintVisitor<R, A> visitor, A arg) {
		return visitor.visit(this, arg);
	}

}