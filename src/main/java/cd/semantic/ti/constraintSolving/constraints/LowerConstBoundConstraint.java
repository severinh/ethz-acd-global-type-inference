package cd.semantic.ti.constraintSolving.constraints;

import com.google.common.collect.ImmutableList;

import cd.semantic.ti.constraintSolving.ConstantTypeSet;
import cd.semantic.ti.constraintSolving.TypeVariable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A lower bound constraint for a type variable v, i.e. L \subseteq v, where L
 * is the lower bound (a constant set).
 */
public class LowerConstBoundConstraint extends TypeConstraint {
	private final TypeVariable typeVariable;
	private final ConstantTypeSet lowerBound;

	public LowerConstBoundConstraint(TypeVariable typeVariable,
			ConstantTypeSet lowerBound,
			ImmutableList<ConstraintCondition> conditions) {
		super(conditions);
		this.typeVariable = checkNotNull(typeVariable);
		this.lowerBound = checkNotNull(lowerBound);
	}

	public TypeVariable getTypeVariable() {
		return typeVariable;
	}

	public ConstantTypeSet getLowerBound() {
		return lowerBound;
	}

	@Override
	public boolean isSatisfied() {
		return !isActive() || lowerBound.isSubsetOf(typeVariable);
	}

	@Override
	public <R, A> R accept(TypeConstraintVisitor<R, A> visitor, A arg) {
		return visitor.visit(this, arg);
	}

	@Override
	public String toString() {
		return buildString(lowerBound + "\u2286" + typeVariable);
	}
}