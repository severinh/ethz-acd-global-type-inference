package cd.semantic.ti.constraintSolving;

import cd.util.NonnullByDefault;

@NonnullByDefault
public interface TypeSetVisitor<R, A> {

	public R visit(ConstantTypeSet constantTypeSet, A arg);

	public R visit(TypeVariable typeVariable, A arg);

}
