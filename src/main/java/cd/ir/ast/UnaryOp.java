package cd.ir.ast;

import cd.ir.ExprVisitor;

public class UnaryOp extends ArgExpr {

	public static enum UOp {

		U_PLUS("+"), U_MINUS("-"), U_BOOL_NOT("!");
		public String repr;

		private UOp(String repr) {
			this.repr = repr;
		}

	}

	public final UnaryOp.UOp operator;

	public UnaryOp(UnaryOp.UOp operator, Expr arg) {
		super(arg);
		this.operator = operator;
	}

	@Override
	public <R, A> R accept(ExprVisitor<R, A> visitor, A arg) {
		return visitor.unaryOp(this, arg);
	}

	@Override
	public UnaryOp deepCopy() {
		return postCopy(new UnaryOp(operator, arg()));
	}

}