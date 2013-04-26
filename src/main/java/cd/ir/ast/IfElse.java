package cd.ir.ast;

import cd.ir.AstVisitor;

public class IfElse extends Stmt {

	public IfElse(Expr cond, Ast then, Ast otherwise) {
		super(3);
		assert cond != null && then != null && otherwise != null;
		setCondition(cond);
		setThen(then);
		setOtherwise(otherwise);
	}

	public Expr condition() {
		return (Expr) this.rwChildren.get(0);
	}

	public void setCondition(Expr node) {
		this.rwChildren.set(0, node);
	}

	public Ast then() {
		return this.rwChildren.get(1);
	}

	public void setThen(Ast node) {
		this.rwChildren.set(1, node);
	}

	public Ast otherwise() {
		return this.rwChildren.get(2);
	}

	public void setOtherwise(Ast node) {
		this.rwChildren.set(2, node);
	}

	@Override
	public <R, A> R accept(AstVisitor<R, A> visitor, A arg) {
		return visitor.ifElse(this, arg);
	}

	@Override
	public Ast deepCopy() {
		return new IfElse((Expr) condition().deepCopy(), then().deepCopy(),
				otherwise().deepCopy());
	}

}