package cd.semantic.ti.constraintSolving.constraints;

import java.util.List;

import cd.semantic.ti.constraintSolving.ConstantTypeSet;
import cd.semantic.ti.constraintSolving.TypeVariable;

/**
 * A lower bound constraint for a type variable v, i.e. L \subseteq v, where L
 * is the lower bound (a constant set)
 */
public class LowerConstBoundConstraint extends TypeConstraint {
	private final TypeVariable typeVariable;
	private final ConstantTypeSet lowerBound;

	public LowerConstBoundConstraint(TypeVariable typeVariable,
			ConstantTypeSet lowerBound, List<ConstraintCondition> conditions) {
		super(conditions);
		this.typeVariable = typeVariable;
		this.lowerBound = lowerBound;
	}

	public TypeVariable getTypeVariable() {
		return typeVariable;
	}

	public ConstantTypeSet getLowerBound() {
		return lowerBound;
	}
}