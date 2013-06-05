package cd.semantic.ti.palsberg.solving;

import cd.util.NonnullByDefault;

@NonnullByDefault
public interface TypeSetVisitor<R, A> {

	public R visit(ConstantTypeSet constantTypeSet, A arg);

	public R visit(TypeVariable typeVariable, A arg);

}
