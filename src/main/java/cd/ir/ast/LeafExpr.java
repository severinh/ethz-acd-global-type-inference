package cd.ir.ast;

/** Base class used for things with no arguments */
abstract class LeafExpr extends Expr {

	public LeafExpr() {
		super(0);
	}

}