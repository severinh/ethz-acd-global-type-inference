package cd.semantic.ti.constraintSolving.constraints;

public interface TypeConstraintVisitor<R, A> {

	public R visitLowerConstBoundConstraint(
			LowerConstBoundConstraint constraint, A arg);

	public R visitUpperConstBoundConstraint(
			UpperConstBoundConstraint constraint, A arg);

	public R visitVariableInequalityConstraint(
			VariableInequalityConstraint constraint, A arg);

}
