package cd.semantic.ti.constraintSolving;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import cd.ir.symbols.TypeSymbol;
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
			ConstraintCondition... conditions) {
		checkNotNull(typeSet);
		checkNotNull(lowerBound);

		addInequality(lowerBound, typeSet, conditions);
	}

	public void addUpperBound(TypeSet typeSet, ConstantTypeSet upperBound,
			ConstraintCondition... conditions) {
		checkNotNull(typeSet);
		checkNotNull(upperBound);

		addInequality(typeSet, upperBound, conditions);
	}

	public void addInequality(TypeSet subTypeSet, TypeSet superTypeSet,
			ConstraintCondition... conditions) {
		checkNotNull(subTypeSet);
		checkNotNull(superTypeSet);

		TypeConstraintBuilder builder = new TypeConstraintBuilder(conditions);
		TypeConstraint constraint = builder.build(subTypeSet, superTypeSet);
		typeConstraints.add(constraint);
	}

	public void addEquality(TypeSet typeSet, TypeSet otherTypeSet,
			ConstraintCondition... conditions) {
		checkNotNull(typeSet);
		checkNotNull(otherTypeSet);

		addInequality(typeSet, otherTypeSet, conditions);
		addInequality(otherTypeSet, typeSet, conditions);
	}

	public TypeVariable addTypeVariable() {
		String newDesc = "typeVar" + typeVariables.size();
		return addTypeVariable(newDesc);
	}

	public TypeVariable addTypeVariable(String varDescription) {
		TypeVariable typeVar = new TypeVariable(varDescription);
		typeVariables.add(typeVar);
		return typeVar;
	}

	@Override
	public String toString() {
		List<String> typeVarContents = new LinkedList<>();
		for (TypeVariable typeVar : typeVariables) {
			String content;
			Set<TypeSymbol> typeVarTypes = typeVar.getTypes();
			if (typeVarTypes.isEmpty()) {
				content = "\u2205";
			} else {
				content = "{" + StringUtils.join(typeVarTypes, ",") + "}";
			}
			typeVarContents.add(typeVar + " = " + content);
		}

		StringBuilder builder = new StringBuilder();
		builder.append("Type variables: \n");
		builder.append(StringUtils.join(typeVarContents, ", "));
		builder.append("\n");
		builder.append("Type constraints: \n");
		builder.append(typeConstraints.toString());
		builder.append("\n");
		return builder.toString();
	}

}