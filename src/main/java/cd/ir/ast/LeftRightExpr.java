package cd.ir.ast;

/**
 * Base class used for exprs with left/right operands. We use this for all
 * expressions that take strictly two operands, such as binary operators or
 * array indexing.
 */
abstract class LeftRightExpr extends Expr {

	public LeftRightExpr(Expr left, Expr right) {
		super(2);
		assert left != null;
		setLeft(left);
		setRight(right);
	}

	public Expr left() {
		return (Expr) this.rwChildren.get(0);
	}

	public void setLeft(Expr node) {
		this.rwChildren.set(0, node);
	}

	public Expr right() {
		return (Expr) this.rwChildren.get(1);
	}

	public void setRight(Expr node) {
		this.rwChildren.set(1, node);
	}

}