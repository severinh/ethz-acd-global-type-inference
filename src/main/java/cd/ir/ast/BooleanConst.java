package cd.ir.ast;

import cd.ir.ExprVisitor;

public class BooleanConst extends LeafExpr {

	public final boolean value;

	public BooleanConst(boolean value) {
		this.value = value;
	}

	@Override
	public <R, A> R accept(ExprVisitor<R, A> visitor, A arg) {
		return visitor.booleanConst(this, arg);
	}

	@Override
	public BooleanConst deepCopy() {
		return postCopy(new BooleanConst(value));
	}

}