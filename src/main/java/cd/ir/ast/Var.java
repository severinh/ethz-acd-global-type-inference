package cd.ir.ast;

import cd.ir.ExprVisitor;
import cd.ir.symbols.VariableSymbol;

public class Var extends LeafExpr {

	private String name;
	private VariableSymbol symbol;

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
	 * it fills in the {@link #type} and {@link #symbol} fields.
	 */
	public static Var withSym(VariableSymbol symbol) {
		Var var = new Var(symbol.name);
		var.symbol = symbol;
		var.setType(symbol.getType());
		return var;
	}

	@Override
	public Var deepCopy() {
		return postCopy(new Var(name));
	}

	@Override
	protected <E extends Expr> E postCopy(E item) {
		((Var) item).symbol = symbol;
		return super.postCopy(item);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public VariableSymbol getSymbol() {
		return symbol;
	}

	public void getSymbol(VariableSymbol symbol) {
		this.symbol = symbol;
	}

	public void setSymbol(VariableSymbol symbol) {
		this.symbol = symbol;
		this.name = symbol.toString();
	}

}