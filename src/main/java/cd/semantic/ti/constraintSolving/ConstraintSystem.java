package cd.semantic.ti.constraintSolving;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.Nonnull;

import cd.semantic.ti.constraintSolving.constraints.ConstraintCondition;
import cd.semantic.ti.constraintSolving.constraints.TypeConstraint;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableSet;

public class ConstraintSystem {

	// all type variables, including the "helper" variables that are used to
	// constraint the types of expressions
	private final Set<TypeVariable> typeVariables = new LinkedHashSet<>();

	// the constraints we impose on type variables
	private final Set<TypeConstraint> typeConstraints = new HashSet<>();

	public ImmutableSet<TypeVariable> getTypeVariables() {
		return ImmutableSet.copyOf(typeVariables);
	}

	public Set<TypeConstraint> getTypeConstraints() {
		return typeConstraints;
	}

	public void addLowerBound(TypeSet typeSet, ConstantTypeSet lowerBound,
			@Nonnull ConstraintCondition... conditions) {
		checkNotNull(typeSet);
		checkNotNull(lowerBound);

		addInequality(lowerBound, typeSet, conditions);
	}

	public void addUpperBound(TypeSet typeSet, ConstantTypeSet upperBound,
			@Nonnull ConstraintCondition... conditions) {
		checkNotNull(typeSet);
		checkNotNull(upperBound);

		addInequality(typeSet, upperBound, conditions);
	}

	public void addInequality(TypeSet subTypeSet, TypeSet superTypeSet,
			@Nonnull ConstraintCondition... conditions) {
		checkNotNull(subTypeSet);
		checkNotNull(superTypeSet);

		TypeConstraintBuilder builder = new TypeConstraintBuilder(conditions);
		TypeConstraint constraint = builder.build(subTypeSet, superTypeSet);
		typeConstraints.add(constraint);
	}

	public void addEquality(TypeSet typeSet, TypeSet otherTypeSet,
			@Nonnull ConstraintCondition... conditions) {
		checkNotNull(typeSet);
		checkNotNull(otherTypeSet);

		addInequality(typeSet, otherTypeSet, conditions);
		addInequality(otherTypeSet, typeSet, conditions);
	}

	public TypeVariable addTypeVariable() {
		String newDesc = "typeVar" + typeVariables.size();
		return addTypeVariable(newDesc);
	}

	public TypeVariable addTypeVariable(@Nonnull String varDescription) {
		TypeVariable typeVar = new TypeVariable(varDescription);
		typeVariables.add(typeVar);
		return typeVar;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		builder.append("Type variables:");
		for (TypeVariable typeVar : typeVariables) {
			builder.append("\n\t");
			builder.append(typeVar);
			builder.append(" = ");
			builder.append(typeVar.getTypeSetString());
		}

		builder.append("\n\nType constraints:");
		for (TypeConstraint typeConstraint : typeConstraints) {
			builder.append("\n\t");
			builder.append(typeConstraint);
		}

		return builder.toString();
	}
}