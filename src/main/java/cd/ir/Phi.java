package cd.ir;

import java.util.ArrayList;
import java.util.List;

import cd.ir.Ast.Expr;
import cd.ir.Symbol.VariableSymbol;

public class Phi {
	public final VariableSymbol v0sym;
	public VariableSymbol lhs;
	public List<Expr> rhs = new ArrayList<Expr>(); // Always an Ast.Var or an Ast.Const!
	
	public Phi(VariableSymbol v0sym, int predCount) {
		this.v0sym = v0sym;
		this.lhs = v0sym;
		for (int i = 0; i < predCount; i++)
			rhs.add(Ast.Var.withSym(v0sym));
	}
	
	@Override
	public String toString() {
		return String.format("<%s = phi%s>", lhs, rhs); 
	}
}
