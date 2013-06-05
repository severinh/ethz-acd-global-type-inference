package cd.semantic.ti.palsberg.constraints;

import javax.annotation.Nullable;

public interface TypeConstraintVisitor<R, A> {

	public R visit(ConstantConstraint constraint, @Nullable A arg);

	public R visit(LowerConstBoundConstraint constraint, @Nullable A arg);

	public R visit(UpperConstBoundConstraint constraint, @Nullable A arg);

	public R visit(VariableInequalityConstraint constraint, @Nullable A arg);

}
