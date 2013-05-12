package cd.semantic.ti.constraintSolving.constraints;

import com.google.common.collect.ImmutableList;
import static com.google.common.base.Preconditions.checkNotNull;

public abstract class TypeConstraint {
	private final ImmutableList<ConstraintCondition> conditions;

	public TypeConstraint(ImmutableList<ConstraintCondition> conditions) {
		this.conditions = checkNotNull(conditions);
	}

	public ImmutableList<ConstraintCondition> getConditions() {
		return conditions;
	}

	public boolean isActive() {
		boolean active = true;
		for (ConstraintCondition cond : conditions) {
			if (!cond.isSatisfied()) {
				active = false;
				break;
			}
		}
		return active;
	}

	public abstract boolean isSatisfied();

	public abstract <R, A> R accept(TypeConstraintVisitor<R, A> visitor, A arg);

}