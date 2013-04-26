package cd.ir.ast;

import cd.ir.AstVisitor;
import cd.ir.symbols.VariableSymbol;

public class VarDecl extends Decl {

	public final String type;
	public final String name;
	public VariableSymbol sym;

	public VarDecl(String type, String name) {
		this(0, type, name);
	}

	protected VarDecl(int num, String type, String name) {
		super(num);
		this.type = type;
		this.name = name;
	}

	@Override
	public <R, A> R accept(AstVisitor<R, A> visitor, A arg) {
		return visitor.varDecl(this, arg);
	}

	@Override
	public Ast deepCopy() {
		return new VarDecl(type, name);
	}

}