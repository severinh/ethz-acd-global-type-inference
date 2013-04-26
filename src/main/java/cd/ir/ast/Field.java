package cd.ir.ast;

import cd.ir.ExprVisitor;
import cd.ir.symbols.VariableSymbol;

public class Field extends ArgExpr {

	public final String fieldName;

	public VariableSymbol sym;

	public Field(Expr arg, String fieldName) {
		super(arg);
		assert arg != null && fieldName != null;
		this.fieldName = fieldName;
	}

	@Override
	public <R, A> R accept(ExprVisitor<R, A> visitor, A arg) {
		return visitor.field(this, arg);
	}

	@Override
	public Field deepCopy() {
		return postCopy(new Field(arg(), fieldName));
	}

	@Override
	protected <E extends Expr> E postCopy(E item) {
		((Field) item).sym = sym;
		return super.postCopy(item);
	}

}