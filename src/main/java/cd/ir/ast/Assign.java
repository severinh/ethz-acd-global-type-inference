package cd.ir.ast;

import cd.ir.AstVisitor;

/**
 * An assignment from {@code right()} to the location represented by
 * {@code left()}.
 */
public class Assign extends Stmt {

	public Assign(Expr left, Expr right) {
		super(2);
		assert left != null && right != null;
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

	@Override
	public <R, A> R accept(AstVisitor<R, A> visitor, A arg) {
		return visitor.assign(this, arg);
	}

	@Override
	public Ast deepCopy() {
		return new Assign((Expr) left().deepCopy(), (Expr) right().deepCopy());
	}

}