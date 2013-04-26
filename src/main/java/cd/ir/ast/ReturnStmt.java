package cd.ir.ast;

import cd.ir.AstVisitor;

public class ReturnStmt extends Stmt {

	public ReturnStmt(Expr arg) {
		super(1);
		setArg(arg);
	}

	public Expr arg() {
		return (Expr) this.rwChildren.get(0);
	}

	public void setArg(Expr node) {
		this.rwChildren.set(0, node);
	}

	@Override
	public <R, A> R accept(AstVisitor<R, A> visitor, A arg) {
		return visitor.returnStmt(this, arg);
	}

	@Override
	public Ast deepCopy() {
		return new ReturnStmt(arg() != null ? (Expr) arg().deepCopy() : null);
	}

}