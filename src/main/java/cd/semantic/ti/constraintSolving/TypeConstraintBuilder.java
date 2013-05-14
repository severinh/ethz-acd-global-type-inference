package cd.semantic.ti.constraintSolving;

import cd.semantic.ti.constraintSolving.constraints.ConstantConstraint;
import cd.semantic.ti.constraintSolving.constraints.ConstraintCondition;
import cd.semantic.ti.constraintSolving.constraints.LowerConstBoundConstraint;
import cd.semantic.ti.constraintSolving.constraints.TypeConstraint;
import cd.semantic.ti.constraintSolving.constraints.UpperConstBoundConstraint;
import cd.semantic.ti.constraintSolving.constraints.VariableInequalityConstraint;

import com.google.common.collect.ImmutableList;

/**
 * Builds type constraints with optional conditions.
 * 
 * It is generic in the sense that it handles type sets of any kind (constant or
 * variable) for both sides of the inequality and determines the right
 * {@link TypeConstraint} subclass using double invocation.
 * 
 * @todo If it is already clear if that the constraint will always be satisfied,
 *       the builder could also return {@link Optional#absent()}.
 */
public class TypeConstraintBuilder {

	private final ImmutableList<ConstraintCondition> conditions;

	public TypeConstraintBuilder(ConstraintCondition... conditions) {
		super();
		this.conditions = ImmutableList.copyOf(conditions);
	}

	/**
	 * Builds a type constraint that requires the given type set to be a subset
	 * of the second given type set.
	 * 
	 * @param subTypeSet
	 * @param superTypeSet
	 * @return the resulting type constraint is absent if it is already certain
	 *         that the constraint will always be satisfied
	 */
	public TypeConstraint build(TypeSet subTypeSet, TypeSet superTypeSet) {
		MasterVisitor masterVisitor = new MasterVisitor();
		return superTypeSet.accept(masterVisitor, subTypeSet);
	}

	private class MasterVisitor implements
			TypeSetVisitor<TypeConstraint, TypeSet> {

		@Override
		public TypeConstraint visit(ConstantTypeSet superSet, TypeSet subSet) {
			ConstantSlaveVisitor visitor = new ConstantSlaveVisitor();
			return subSet.accept(visitor, superSet);
		}

		@Override
		public TypeConstraint visit(TypeVariable superSet, TypeSet subSet) {
			VariableSlaveVisitor visitor = new VariableSlaveVisitor();
			return subSet.accept(visitor, superSet);
		}

	}

	private class ConstantSlaveVisitor implements
			TypeSetVisitor<TypeConstraint, ConstantTypeSet> {

		@Override
		public TypeConstraint visit(ConstantTypeSet subSet,
				ConstantTypeSet superSet) {
			return new ConstantConstraint(subSet, superSet, conditions);
		}

		@Override
		public TypeConstraint visit(TypeVariable subSet,
				ConstantTypeSet superSet) {
			return new UpperConstBoundConstraint(subSet, superSet, conditions);
		}
	}

	private class VariableSlaveVisitor implements
			TypeSetVisitor<TypeConstraint, TypeVariable> {

		@Override
		public TypeConstraint visit(ConstantTypeSet subSet,
				TypeVariable superSet) {
			return new LowerConstBoundConstraint(superSet, subSet, conditions);
		}

		@Override
		public TypeConstraint visit(TypeVariable subSet, TypeVariable superSet) {
			return new VariableInequalityConstraint(subSet, superSet,
					conditions);
		}

	}

}