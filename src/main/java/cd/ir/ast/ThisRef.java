package cd.ir.ast;

import cd.ir.ExprVisitor;

/** Represents {@code this}, the current object */
public class ThisRef extends LeafExpr {

	@Override
	public <R, A> R accept(ExprVisitor<R, A> visitor, A arg) {
		return visitor.thisRef(this, arg);
	}

	@Override
	public ThisRef deepCopy() {
		return postCopy(new ThisRef());
	}

}