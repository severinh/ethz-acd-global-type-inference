package cd.ir.ast;

import cd.ir.AstVisitor;

public class BuiltInWriteln extends Stmt {

	public BuiltInWriteln() {
		super(0);
	}

	@Override
	public <R, A> R accept(AstVisitor<R, A> visitor, A arg) {
		return visitor.builtInWriteln(this, arg);
	}

	@Override
	public Ast deepCopy() {
		return new BuiltInWriteln();
	}

}