package cd.ir.ast;

import cd.ir.ExprVisitor;
import cd.ir.symbols.VariableSymbol;

public class Var extends LeafExpr {

	public String name;
	public VariableSymbol sym;

	/**
	 * Use this constructor to build an instance of this AST in the parser.
	 */
	public Var(String name) {
		this.name = name;
	}

	@Override
	public <R, A> R accept(ExprVisitor<R, A> visitor, A arg) {
		return visitor.var(this, arg);
	}

	/**
	 * Use this static function to build an instance after the semantic phase;
	 * it fills in the {@link #type} and {@link #sym} fields.
	 */
	public static Var withSym(VariableSymbol sym) {
		Var v = new Var(sym.name);
		v.sym = sym;
		v.type = sym.getType();
		return v;
	}

	@Override
	public Var deepCopy() {
		return postCopy(new Var(name));
	}

	@Override
	protected <E extends Expr> E postCopy(E item) {
		((Var) item).sym = sym;
		return super.postCopy(item);
	}

	public void setSymbol(VariableSymbol variableSymbol) {
		sym = variableSymbol;
		name = sym.toString();
	}

}