package cd.ir.ast;

import cd.ir.ExprVisitor;

public class BuiltInReadFloat extends LeafExpr {

	@Override
	public <R, A> R accept(ExprVisitor<R, A> visitor, A arg) {
		return visitor.builtInReadFloat(this, arg);
	}

	@Override
	public BuiltInReadFloat deepCopy() {
		return postCopy(new BuiltInReadFloat());
	}

}