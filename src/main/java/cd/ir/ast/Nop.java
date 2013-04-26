package cd.ir.ast;

import cd.ir.AstVisitor;

/** Represents an empty statement: has no effect. */
public class Nop extends Stmt {

	public Nop() {
		super(0);
	}

	@Override
	public <R, A> R accept(AstVisitor<R, A> visitor, A arg) {
		return visitor.nop(this, arg);
	}

	@Override
	public Ast deepCopy() {
		return new Nop();
	}

}