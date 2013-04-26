package cd.ir.ast;

/** Base class used for expressions with a single argument */
abstract class ArgExpr extends Expr {

	public ArgExpr(Expr arg) {
		super(1);
		assert arg != null;
		setArg(arg);
	}

	public Expr arg() {
		return (Expr) this.rwChildren.get(0);
	}

	public void setArg(Expr node) {
		this.rwChildren.set(0, node);
	}

}