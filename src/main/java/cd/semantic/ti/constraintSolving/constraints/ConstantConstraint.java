package cd.semantic.ti.constraintSolving.constraints;

import cd.semantic.ti.constraintSolving.ConstantTypeSet;

import com.google.common.collect.ImmutableList;

public class ConstantConstraint extends TypeConstraint {

	private final ConstantTypeSet subTypeSet;
	private final ConstantTypeSet superTypeSet;
	private final boolean isSatisfiedIfActive;

	public ConstantConstraint(ConstantTypeSet subTypeSet,
			ConstantTypeSet superTypeSet,
			ImmutableList<ConstraintCondition> conditions) {
		super(conditions);
		this.subTypeSet = subTypeSet;
		this.superTypeSet = superTypeSet;
		this.isSatisfiedIfActive = subTypeSet.isSubsetOf(superTypeSet);
	}

	@Override
	public ConstantTypeSet getSubTypeSet() {
		return subTypeSet;
	}

	@Override
	public ConstantTypeSet getSuperTypeSet() {
		return superTypeSet;
	}

	@Override
	public boolean isSatisfied() {
		return !isActive() || isSatisfiedIfActive;
	}

	@Override
	public <R, A> R accept(TypeConstraintVisitor<R, A> visitor, A arg) {
		return visitor.visit(this, arg);
	}

}
