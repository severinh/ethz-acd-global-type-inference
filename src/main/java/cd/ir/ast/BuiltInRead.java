package cd.ir.ast;

import cd.ir.ExprVisitor;

public class BuiltInRead extends LeafExpr {

	@Override
	public <R, A> R accept(ExprVisitor<R, A> visitor, A arg) {
		return visitor.builtInRead(this, arg);
	}

	@Override
	public BuiltInRead deepCopy() {
		return postCopy(new BuiltInRead());
	}

}