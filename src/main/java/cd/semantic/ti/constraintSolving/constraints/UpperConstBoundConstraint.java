package cd.semantic.ti.constraintSolving.constraints;

import com.google.common.collect.ImmutableList;

import cd.semantic.ti.constraintSolving.ConstantTypeSet;
import cd.semantic.ti.constraintSolving.TypeVariable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An upper bound constraint for a type variable v, i.e. v \subseteq U, where U
 * is the upper bound (a constant set)
 */
public class UpperConstBoundConstraint extends TypeConstraint {
	private final TypeVariable typeVariable;
	private final ConstantTypeSet upperBound;

	public UpperConstBoundConstraint(TypeVariable typeVariable,
			ConstantTypeSet upperBound,
			ImmutableList<ConstraintCondition> conditions) {
		super(conditions);
		this.typeVariable = checkNotNull(typeVariable);
		this.upperBound = checkNotNull(upperBound);
	}

	public TypeVariable getTypeVariable() {
		return typeVariable;
	}

	public ConstantTypeSet getUpperBound() {
		return upperBound;
	}
}