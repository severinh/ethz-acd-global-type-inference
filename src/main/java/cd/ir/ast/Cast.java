package cd.ir.ast;

import cd.ir.ExprVisitor;
import cd.ir.symbols.TypeSymbol;

/** A Cast from one type to another: {@code (typeName)arg} */
public class Cast extends ArgExpr {

	public final String typeName;
	public TypeSymbol typeSym;

	public Cast(Expr arg, String typeName) {
		super(arg);
		this.typeName = typeName;
	}

	@Override
	public <R, A> R accept(ExprVisitor<R, A> visitor, A arg) {
		return visitor.cast(this, arg);
	}

	@Override
	public Cast deepCopy() {
		return postCopy(new Cast(arg(), typeName));
	}

	@Override
	protected <E extends Expr> E postCopy(E item) {
		((Cast) item).typeSym = typeSym;
		return super.postCopy(item);
	}

}