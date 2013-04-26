package cd.ir.symbols;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cd.ir.Ast;

public class MethodSymbol extends Symbol {

	public final Ast.MethodDecl ast;
	public final Map<String, VariableSymbol> locals = new HashMap<>();
	public final List<VariableSymbol> parameters = new ArrayList<>();
	public final ClassSymbol owner;

	public TypeSymbol returnType;
	public int vtableIndex = -1;
	public MethodSymbol overrides;

	public MethodSymbol(Ast.MethodDecl ast, ClassSymbol owner) {
		super(ast.name);
		this.ast = ast;
		this.owner = owner;
	}

	@Override
	public String toString() {
		return name + "(...)";
	}

}