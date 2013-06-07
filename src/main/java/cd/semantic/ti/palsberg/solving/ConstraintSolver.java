package cd.semantic.ti.palsberg.solving;

import java.util.Set;

import cd.ir.symbols.TypeSymbol;
import cd.semantic.TypeSymbolTable;
import cd.semantic.ti.palsberg.constraints.ConstantConstraint;
import cd.semantic.ti.palsberg.constraints.LowerConstBoundConstraint;
import cd.semantic.ti.palsberg.constraints.TypeConstraint;
import cd.semantic.ti.palsberg.constraints.TypeConstraintVisitor;
import cd.semantic.ti.palsberg.constraints.UpperConstBoundConstraint;
import cd.semantic.ti.palsberg.constraints.VariableInequalityConstraint;
import static com.google.common.base.Preconditions.checkNotNull;

public class ConstraintSolver {

	private final TypeSymbolTable typeSymbols;
	private final ConstraintSystem constraintSystem;
	private boolean hasSolution;

	public ConstraintSolver(TypeSymbolTable typeSymbols,
			ConstraintSystem constraints) {
		this.typeSymbols = checkNotNull(typeSymbols);
		this.constraintSystem = checkNotNull(constraints);
	}

	public boolean hasSolution() {
		return hasSolution;
	}

	public void solve() {
		hasSolution = true;
		boolean changed;
		UnsatisfiedConstraintUpdater variableUpdater = new UnsatisfiedConstraintUpdater();
		Set<TypeConstraint> constraints = constraintSystem.getTypeConstraints();
		do {
			changed = false;
			for (TypeConstraint constraint : constraints) {
				if (!constraint.isSatisfied()) {
					constraint.accept(variableUpdater, null);
					changed = true;
				}
			}
		} while (changed && hasSolution);
	}

	private class UnsatisfiedConstraintUpdater implements
			TypeConstraintVisitor<Void, Void> {

		@Override
		public Void visit(LowerConstBoundConstraint constraint, Void arg) {
			constraint.getTypeVariable().extend(constraint.getLowerBound());
			// Add the LCA of the resulting set. We need to decide on a single
			// static type at the end of type inference anyway. Since the update
			// is monotonic, i.e., types are only added but not removed from the
			// type variable, it is safe to add the LCA.
			// NOTE: Does not add "intermediate" types between the types in the
			// set and the LCA. Should not be a problem.
			TypeSymbol lca = typeSymbols.getLCA(constraint.getTypeVariable()
					.getTypes());
			constraint.getTypeVariable().extend(lca);
			return null;
		}

		@Override
		public Void visit(UpperConstBoundConstraint constraint, Void arg) {
			hasSolution = false;
			return null;
		}

		@Override
		public Void visit(VariableInequalityConstraint constraint, Void arg) {
			constraint.getSuperTypeSet().extend(constraint.getSubTypeSet());
			// Add the LCA of the resulting set. We need to decide on a single
			// static type at the end of type inference anyway. Since the update
			// is monotonic, i.e., types are only added but not removed from the
			// type variable, it is safe to add the LCA.
			// NOTE: Does not add "intermediate" types between the types in the
			// set and the LCA. Should not be a problem.
			TypeSymbol lca = typeSymbols.getLCA(constraint.getSuperTypeSet()
					.getTypes());
			constraint.getSuperTypeSet().extend(lca);
			return null;
		}

		@Override
		public Void visit(ConstantConstraint constraint, Void arg) {
			hasSolution = false;
			return null;
		}

	}

}