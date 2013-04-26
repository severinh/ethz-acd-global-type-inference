package cd.ir.ast;

import cd.ir.ExprVisitor;

public class FloatConst extends LeafExpr {

	public final float value;

	public FloatConst(float value) {
		this.value = value;
	}

	@Override
	public <R, A> R accept(ExprVisitor<R, A> visitor, A arg) {
		return visitor.floatConst(this, arg);
	}

	@Override
	public FloatConst deepCopy() {
		return postCopy(new FloatConst(value));
	}

}