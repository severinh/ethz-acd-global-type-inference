package cd.semantic.ti.constraintSolving;

public interface TypeSetVisitor<R, A> {

	public R visit(ConstantTypeSet constantTypeSet, A arg);

	public R visit(TypeVariable typeVariable, A arg);

}
