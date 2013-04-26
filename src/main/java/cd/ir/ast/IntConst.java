package cd.ir.ast;

import cd.ir.ExprVisitor;

public class IntConst extends LeafExpr {

	public final int value;

	public IntConst(int value) {
		this.value = value;
	}

	@Override
	public <R, A> R accept(ExprVisitor<R, A> visitor, A arg) {
		return visitor.intConst(this, arg);
	}

	@Override
	public IntConst deepCopy() {
		return postCopy(new IntConst(value));
	}

}