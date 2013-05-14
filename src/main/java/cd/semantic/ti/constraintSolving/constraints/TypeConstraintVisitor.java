package cd.semantic.ti.constraintSolving.constraints;

public interface TypeConstraintVisitor<R, A> {

	public R visit(ConstantConstraint constraint, A arg);

	public R visit(LowerConstBoundConstraint constraint, A arg);

	public R visit(UpperConstBoundConstraint constraint, A arg);

	public R visit(VariableInequalityConstraint constraint, A arg);

}
