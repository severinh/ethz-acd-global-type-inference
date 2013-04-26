package cd.ir.ast;

import cd.ir.ExprVisitor;

public class NullConst extends LeafExpr {

	@Override
	public <R, A> R accept(ExprVisitor<R, A> visitor, A arg) {
		return visitor.nullConst(this, arg);
	}

	@Override
	public NullConst deepCopy() {
		return postCopy(new NullConst());
	}

}