package cd.ir.ast;

import cd.ir.AstVisitor;

public class BuiltInTock extends Stmt {

	public BuiltInTock() {
		super(0);
	}

	@Override
	public <R, A> R accept(AstVisitor<R, A> visitor, A arg) {
		return visitor.builtInTock(this, arg);
	}

	@Override
	public Ast deepCopy() {
		return new BuiltInTock();
	}
}