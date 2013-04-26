package cd.ir.ast;

import cd.ir.ExprVisitor;

/**
 * A binary operation combining a left and right operand, such as "1+2" or "3*4"
 */
public class BinaryOp extends LeftRightExpr {

	public static enum BOp {

		B_TIMES("*"), B_DIV("/"), B_MOD("%"), B_PLUS("+"), B_MINUS("-"), B_AND(
				"&&"), B_OR("||"), B_EQUAL("=="), B_NOT_EQUAL("!="), B_LESS_THAN(
				"<"), B_LESS_OR_EQUAL("<="), B_GREATER_THAN(">"), B_GREATER_OR_EQUAL(
				">=");

		public String repr;

		private BOp(String repr) {
			this.repr = repr;
		}

	}

	public final BinaryOp.BOp operator;

	public BinaryOp(Expr left, BinaryOp.BOp operator, Expr right) {
		super(left, right);
		this.operator = operator;
	}

	@Override
	public <R, A> R accept(ExprVisitor<R, A> visitor, A arg) {
		return visitor.binaryOp(this, arg);
	}

	@Override
	public BinaryOp deepCopy() {
		return postCopy(new BinaryOp(left(), operator, right()));
	}

}