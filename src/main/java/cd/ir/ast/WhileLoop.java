package cd.ir.ast;

import cd.ir.AstVisitor;

public class WhileLoop extends Stmt {

	public WhileLoop(Expr condition, Ast body) {
		super(2);
		assert condition != null && body != null;
		setCondition(condition);
		setBody(body);
	}

	public Expr condition() {
		return (Expr) this.rwChildren.get(0);
	}

	public void setCondition(Expr cond) {
		this.rwChildren.set(0, cond);
	}

	public Ast body() {
		return this.rwChildren.get(1);
	}

	public void setBody(Ast body) {
		this.rwChildren.set(1, body);
	}

	@Override
	public <R, A> R accept(AstVisitor<R, A> visitor, A arg) {
		return visitor.whileLoop(this, arg);
	}

	@Override
	public Ast deepCopy() {
		return new WhileLoop((Expr) condition().deepCopy(), body().deepCopy());
	}
}