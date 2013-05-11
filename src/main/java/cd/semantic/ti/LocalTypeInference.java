package cd.semantic.ti;

import cd.CompilationContext;
import cd.ir.AstVisitor;
import cd.ir.ast.ClassDecl;
import cd.ir.ast.Expr;
import cd.ir.ast.MethodDecl;
import cd.ir.symbols.VariableSymbol;
import cd.semantic.ExprTypingVisitor;
import cd.semantic.SymbolTable;
import cd.semantic.TypeSymbolTable;

/**
 * Base class for local type inference.
 */
public abstract class LocalTypeInference implements TypeInference {

	public LocalTypeInference() {
		super();
	}

	@Override
	public void inferTypes(CompilationContext context) {
		final TypeSymbolTable typeSymbols = context.getTypeSymbols();
		final ExprTypingVisitor exprTypingVisitor = new ExprTypingVisitor(
				typeSymbols);

		for (ClassDecl classDecl : context.getAstRoots()) {
			for (final MethodDecl mdecl : classDecl.methods()) {
				// Run the expression typing visitor over the method once,
				// such that type symbols are set to something non-null.
				final SymbolTable<VariableSymbol> scope = mdecl.sym.getScope();
				mdecl.accept(new AstVisitor<Void, Void>() {

					@Override
					protected Void dfltExpr(Expr expr, Void arg) {
						exprTypingVisitor.type(expr, scope);
						return null;
					}

				}, null);

				inferTypes(mdecl, typeSymbols);
			}
		}
	}

	/**
	 * Infer types of all local variable symbols in the given method, based on
	 * the given type symbol table.
	 * <p>
	 * Subclasses implementing this method can take for granted that the
	 * expressions in the passed method have already been typed, i.e., the
	 * references to type symbols are non-null (bottom for unknown types).
	 */
	public abstract void inferTypes(MethodDecl mdecl,
			TypeSymbolTable typeSymbols);

}