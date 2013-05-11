package cd.semantic.ti.constraintSolving;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import cd.semantic.ti.constraintSolving.constraints.ConstraintCondition;
import cd.semantic.ti.constraintSolving.constraints.LowerConstBoundConstraint;
import cd.semantic.ti.constraintSolving.constraints.UpperConstBoundConstraint;
import cd.semantic.ti.constraintSolving.constraints.VariableInequalityConstraint;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

public class ConstraintSystem {
	// all type variables, including the "helper" variables that are used to
	// constraint the types of expressions
	private final Set<TypeVariable> typeVariables = new LinkedHashSet<>();

	// the constraints we impose on type variables
	private final Set<VariableInequalityConstraint> variableInequalityConstraints = new HashSet<>();
	private final Set<LowerConstBoundConstraint> lowerBoundConstraints = new HashSet<>();
	private final Set<UpperConstBoundConstraint> upperBoundConstraints = new HashSet<>();

	public ImmutableSet<TypeVariable> getTypeVariables() {
		return ImmutableSet.copyOf(typeVariables);
	}

	public Set<VariableInequalityConstraint> getVariableInequalityConstraints() {
		return variableInequalityConstraints;
	}

	public Set<LowerConstBoundConstraint> getLowerBoundConstraints() {
		return lowerBoundConstraints;
	}

	public Set<UpperConstBoundConstraint> getUpperBoundConstraints() {
		return upperBoundConstraints;
	}

	public LowerConstBoundConstraint addLowerBound(TypeVariable var,
			ConstantTypeSet lowerBound, ConstraintCondition... conditions) {
		checkNotNull(var);
		checkNotNull(lowerBound);

		LowerConstBoundConstraint result = new LowerConstBoundConstraint(var,
				lowerBound, ImmutableList.copyOf(conditions));
		lowerBoundConstraints.add(result);
		return result;
	}

	public UpperConstBoundConstraint addUpperBound(TypeVariable var,
			ConstantTypeSet upperBound, ConstraintCondition... conditions) {
		checkNotNull(var);
		checkNotNull(upperBound);

		UpperConstBoundConstraint result = new UpperConstBoundConstraint(var,
				upperBound, ImmutableList.copyOf(conditions));
		upperBoundConstraints.add(result);
		return result;
	}

	public void addVarInequality(TypeVariable var1, TypeVariable var2,
			ConstraintCondition... conditions) {
		checkNotNull(var1);
		checkNotNull(var2);

		ImmutableList<ConstraintCondition> conds = ImmutableList
				.copyOf(conditions);
		VariableInequalityConstraint constraint = new VariableInequalityConstraint(
				var1, var2, conds);
		variableInequalityConstraints.add(constraint);
	}

	public void addConstEquality(TypeVariable var, ConstantTypeSet constSet,
			ConstraintCondition... conditions) {
		checkNotNull(var);
		checkNotNull(constSet);

		addLowerBound(var, constSet, conditions);
		addUpperBound(var, constSet, conditions);
	}

	public void addVarEquality(TypeVariable var1, TypeVariable var2,
			ConstraintCondition... conditions) {
		checkNotNull(var1);
		checkNotNull(var2);

		addVarInequality(var1, var2, conditions);
		addVarInequality(var2, var1, conditions);
	}

	public TypeVariable addTypeVariable() {
		TypeVariable typeVar = new TypeVariable();
		typeVariables.add(typeVar);
		return typeVar;
	}

}