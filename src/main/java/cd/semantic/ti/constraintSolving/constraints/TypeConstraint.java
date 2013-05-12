package cd.semantic.ti.constraintSolving.constraints;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

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

	protected String buildString(String inequalityString) {
		if (conditions.isEmpty()) {
			return inequalityString;
		} else {
			List<String> conditionStrings = new ArrayList<>(conditions.size());
			for (ConstraintCondition condition : conditions) {
				conditionStrings.add("(" + condition + ")");
			}
			return StringUtils.join(conditionStrings, "\u2227") + "\u21D2("
					+ inequalityString + ")";
		}

	}
}