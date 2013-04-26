package cd.ir.ast;

import cd.ir.AstVisitor;

public class BuiltInWrite extends Stmt {

	public BuiltInWrite(Expr arg) {
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

	@Override
	public <R, A> R accept(AstVisitor<R, A> visitor, A arg) {
		return visitor.builtInWrite(this, arg);
	}

	@Override
	public Ast deepCopy() {
		return new BuiltInWrite((Expr) arg().deepCopy());
	}

}