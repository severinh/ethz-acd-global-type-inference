package cd.semantic.ti.constraintSolving;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


import cd.semantic.ti.constraintSolving.constraints.ConstraintCondition;
import cd.semantic.ti.constraintSolving.constraints.LowerConstBoundConstraint;
import cd.semantic.ti.constraintSolving.constraints.UpperConstBoundConstraint;
import cd.semantic.ti.constraintSolving.constraints.VariableInequalityConstraint;

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

	public LowerConstBoundConstraint addLowerBound(TypeVariable var,
			ConstantTypeSet lowerBound, ConstraintCondition... conditions) {
		LowerConstBoundConstraint result = new LowerConstBoundConstraint(
				var, lowerBound, Arrays.asList(conditions));
		lowerBoundConstraints.add(result);
		return result;
	}

	public UpperConstBoundConstraint addUpperBound(TypeVariable var,
			ConstantTypeSet upperBound, ConstraintCondition... conditions) {
		UpperConstBoundConstraint result = new UpperConstBoundConstraint(
				var, upperBound, Arrays.asList(conditions));
		upperBoundConstraints.add(result);
		return result;
	}

	public void addConstEquality(TypeVariable var,
			ConstantTypeSet constSet, ConstraintCondition... conditions) {
		LowerConstBoundConstraint low = new LowerConstBoundConstraint(var,
				constSet, Arrays.asList(conditions));
		lowerBoundConstraints.add(low);
		UpperConstBoundConstraint up = new UpperConstBoundConstraint(var,
				constSet, Arrays.asList(conditions));
		upperBoundConstraints.add(up);
	}

	public void addVarEquality(TypeVariable var1, TypeVariable var2,
			ConstraintCondition... conditions) {
		List<ConstraintCondition> constConditions = Arrays.asList(conditions);
		VariableInequalityConstraint low = new VariableInequalityConstraint(
				var1, var2, constConditions);
		variableInequalityConstraints.add(low);
		VariableInequalityConstraint up = new VariableInequalityConstraint(
				var2, var1, constConditions);
		variableInequalityConstraints.add(up);
	}

	public TypeVariable addTypeVariable() {
		TypeVariable typeVar = new TypeVariable();
		typeVariables.add(typeVar);
		return typeVar;
	}
}