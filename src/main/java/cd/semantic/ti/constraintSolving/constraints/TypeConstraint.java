package cd.semantic.ti.constraintSolving.constraints;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;

import cd.semantic.ti.constraintSolving.TypeSet;

import com.google.common.collect.ImmutableList;

public abstract class TypeConstraint {

	private final ImmutableList<ConstraintCondition> conditions;

	public TypeConstraint(ImmutableList<ConstraintCondition> conditions) {
		this.conditions = conditions;
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

	public boolean isSatisfied() {
		return !isActive() || getSubTypeSet().isSubsetOf(getSuperTypeSet());
	}

	public abstract <R, A> R accept(TypeConstraintVisitor<R, A> visitor,
			@Nullable A arg);

	public abstract TypeSet getSubTypeSet();

	public abstract TypeSet getSuperTypeSet();

	@Override
	public String toString() {
		String inequality = getSubTypeSet() + " \u2286 " + getSuperTypeSet();
		if (conditions.isEmpty()) {
			return inequality;
		} else {
			List<String> conditionStrings = new ArrayList<>(conditions.size());
			for (ConstraintCondition condition : conditions) {
				conditionStrings.add("(" + condition + ")");
			}
			return StringUtils.join(conditionStrings, " \u2227 ") + " \u21D2 ("
					+ inequality + ")";
		}
	}

}