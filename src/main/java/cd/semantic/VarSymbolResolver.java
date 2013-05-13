package cd.semantic;

import cd.exceptions.SemanticFailure;
import cd.exceptions.SemanticFailure.Cause;
import cd.ir.AstVisitor;
import cd.ir.ast.MethodDecl;
import cd.ir.ast.Var;
import cd.ir.symbols.VariableSymbol;

/**
 * Associates each {@link Var} expression in {@link MethodDecl}s with the
 * corresponding {@link VariableSymbol}, which may either represent a local
 * variable or a field of the class that the method is declared in.
 * 
 * The resolution process can only take place after all symbols have already
 * been created. Besides the symbols, it does not require any kind of
 * information. So the resolver is also expected to work in the face of both
 * globally and locally missing type annotations.
 */
public final class VarSymbolResolver {

	/**
	 * Resolves all {@link Var} expressions in the given method.
	 * 
	 * @param methodDecl
	 *            the method declaration, which must already be associated by a
	 *            fully-constructed method symbol
	 */
	public void resolveVars(MethodDecl methodDecl) {
		SymbolTable<VariableSymbol> scope = methodDecl.sym.getScope();
		VarSymbolResolverVisitor visitor = new VarSymbolResolverVisitor();

		methodDecl.accept(visitor, scope);
	}

	private static class VarSymbolResolverVisitor extends
			AstVisitor<Void, SymbolTable<VariableSymbol>> {

		@Override
		public Void var(Var var, SymbolTable<VariableSymbol> scope) {
			VariableSymbol symbol = scope.get(var.getName());
			if (symbol == null) {
				throw new SemanticFailure(Cause.NO_SUCH_VARIABLE,
						"No variable %s was found", var.getName());
			}
			var.setSymbol(symbol);
			return null;
		}

	}

}
