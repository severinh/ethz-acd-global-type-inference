package cd.ir.ast;

import cd.ir.ExprVisitor;

public class Index extends LeftRightExpr {

	public Index(Expr array, Expr index) {
		super(array, index);
	}

	@Override
	public <R, A> R accept(ExprVisitor<R, A> visitor, A arg) {
		return visitor.index(this, arg);
	}

	@Override
	public Index deepCopy() {
		return postCopy(new Index(left(), right()));
	}

}