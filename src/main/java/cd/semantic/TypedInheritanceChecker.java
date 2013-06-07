package cd.semantic;

import cd.exceptions.SemanticFailure;
import cd.exceptions.SemanticFailure.Cause;
import cd.ir.ast.ClassDecl;
import cd.ir.ast.MethodDecl;
import cd.ir.symbols.MethodSymbol;
import cd.ir.symbols.VariableSymbol;
import cd.ir.AstVisitor;
import cd.util.Pair;

/**
 * Detects inheritance errors in an AST with only typed variables.
 * 
 * In particular, it checks that the parameter and return types of overriding
 * methods match.
 */
public class TypedInheritanceChecker extends AstVisitor<Void, Void> {

	@Override
	public Void classDecl(ClassDecl ast, Void arg) {
		return this.visitChildren(ast, null);
	}

	@Override
	public Void methodDecl(MethodDecl ast, Void arg) {
		// check that methods overridden from a parent class agree
		// on number/type of parameters
		MethodSymbol sym = ast.sym;
		if (sym.getOverriddenMethod().isPresent()) {
			MethodSymbol superSym = sym.getOverriddenMethod().get();
			for (Pair<VariableSymbol> pair : Pair.zip(sym.getParameters(),
					superSym.getParameters()))
				if (pair.a.getType() != pair.b.getType())
					throw new SemanticFailure(
							Cause.INVALID_OVERRIDE,
							"Method parameter %s has type %s, but "
									+ "corresponding base class parameter %s has type %s",
							pair.a.name, pair.a.getType(), pair.b.name, pair.b
									.getType());
			if (superSym.returnType != sym.returnType)
				throw new SemanticFailure(Cause.INVALID_OVERRIDE,
						"Overridden method %s has return type %s,"
								+ "but its superclass has %s", ast.name,
						sym.returnType, superSym.returnType);
		}

		return null;
	}

}
