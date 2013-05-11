package cd.semantic.ti.constraintSolving.constraints;

import java.util.Set;

import cd.ir.symbols.TypeSymbol;
import cd.semantic.ti.constraintSolving.TypeVariable;

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
			TypeVariable var = cond.getTypeVariable();
			Set<TypeSymbol> availableTypes = var.getTypes();
			if (!availableTypes.contains(cond.getTypeAtom())) {
				active = false;
				break;
			}
		}
		return active;
	}
}