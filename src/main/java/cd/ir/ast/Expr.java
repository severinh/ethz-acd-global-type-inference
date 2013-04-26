package cd.ir.ast;

import cd.ir.AstVisitor;
import cd.ir.ExprVisitor;
import cd.ir.symbols.TypeSymbol;

/** Base class for all expressions */
public abstract class Expr extends Ast {

	protected Expr(int fixedCount) {
		super(fixedCount);
	}

	/**
	 * Type that this expression will evaluate to (computed in semantic phase).
	 */
	public TypeSymbol type;

	@Override
	public <R, A> R accept(AstVisitor<R, A> visitor, A arg) {
		return this.accept((ExprVisitor<R, A>) visitor, arg);
	}

	public abstract <R, A> R accept(ExprVisitor<R, A> visitor, A arg);

	/** Copies any non-AST fields. */
	protected <E extends Expr> E postCopy(E item) {
		item.type = type;
		return item;
	}

}