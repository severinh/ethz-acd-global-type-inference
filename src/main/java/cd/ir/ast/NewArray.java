package cd.ir.ast;

import cd.ir.ExprVisitor;

public class NewArray extends ArgExpr {

	/** Name of the type to be created: must be an array type */
	public String typeName;

	public NewArray(String typeName, Expr capacity) {
		super(capacity);
		this.typeName = typeName;
	}

	@Override
	public <R, A> R accept(ExprVisitor<R, A> visitor, A arg) {
		return visitor.newArray(this, arg);
	}

	@Override
	public NewArray deepCopy() {
		return postCopy(new NewArray(typeName, arg()));
	}

}