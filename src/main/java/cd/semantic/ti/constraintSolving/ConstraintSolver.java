package cd.semantic.ti.constraintSolving;

import cd.semantic.ti.constraintSolving.constraints.LowerConstBoundConstraint;
import cd.semantic.ti.constraintSolving.constraints.UpperConstBoundConstraint;
import cd.semantic.ti.constraintSolving.constraints.VariableInequalityConstraint;

public class ConstraintSolver {
	private final ConstraintSystem constraintSystem;
	private boolean hasSolution;

	public ConstraintSolver(ConstraintSystem constraints) {
		this.constraintSystem = constraints;
	}
	
	public boolean hasSolution() {
		return hasSolution;
	}

	public void solve() {
		hasSolution = true;
		boolean changed = false;
		do {
			for (LowerConstBoundConstraint constraint : constraintSystem.getLowerBoundConstraints()) {
				if (constraint.isActive()) {
					TypeVariable typeVar = constraint.getTypeVariable();
					ConstantTypeSet lowerBound = constraint.getLowerBound();
					if (!lowerBound.isSubsetOf(typeVar)) {
						typeVar.extend(lowerBound);
						changed = true;
					}
				}
			}
			
			for (VariableInequalityConstraint constraint : constraintSystem.getVariableInequalityConstraints()) {
				if (constraint.isActive()) {			
					TypeVariable rightVar = constraint.getRight();
					TypeVariable leftVar = constraint.getLeft();
					if (!leftVar.isSubsetOf(rightVar)) {
						rightVar.extend(leftVar);
						changed = true;
					}
				}
			}
			
			for (UpperConstBoundConstraint constraint : constraintSystem.getUpperBoundConstraints()) {
				if (constraint.isActive()) {
					ConstantTypeSet upperBound = constraint.getUpperBound();
					TypeVariable typeVar = constraint.getTypeVariable();
					if (typeVar.isSubsetOf(upperBound)) {
						hasSolution = false;
						changed = false;
						break;
					}
				}
			}
		} while (changed);
	}
}