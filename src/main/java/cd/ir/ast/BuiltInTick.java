package cd.ir.ast;

import cd.ir.AstVisitor;

public class BuiltInTick extends Stmt {

	public BuiltInTick() {
		super(0);
	}

	@Override
	public <R, A> R accept(AstVisitor<R, A> visitor, A arg) {
		return visitor.builtInTick(this, arg);
	}

	@Override
	public Ast deepCopy() {
		return new BuiltInTick();
	}
}