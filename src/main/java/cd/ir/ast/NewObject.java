package cd.ir.ast;

import cd.ir.ExprVisitor;

public class NewObject extends LeafExpr {

	/** Name of the type to be created */
	public String typeName;

	public NewObject(String typeName) {
		this.typeName = typeName;
	}

	@Override
	public <R, A> R accept(ExprVisitor<R, A> visitor, A arg) {
		return visitor.newObject(this, arg);
	}

	@Override
	public NewObject deepCopy() {
		return postCopy(new NewObject(typeName));
	}

}