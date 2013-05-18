package cd.semantic;

import cd.exceptions.SemanticFailure;
import cd.exceptions.SemanticFailure.Cause;
import cd.ir.ast.ClassDecl;
import cd.ir.ast.MethodDecl;
import cd.ir.symbols.MethodSymbol;
import cd.ir.AstVisitor;

/**
 * Detects inheritance errors in an AST with potentially untyped variables.
 * 
 * In particular, it detects circular inheritance and inconsistent number of
 * parameters in the case of overriding methods.
 * 
 * It does however not check that the parameter and return types of overriding
 * methods match, since they may not be typed yet.
 * 
 * @todo find a better class name
 */
public class UntypedInheritanceChecker extends AstVisitor<Void, Void> {

	public void check(ClassDecl classDecl) {
		visit(classDecl, null);
	}

	@Override
	public Void classDecl(ClassDecl ast, Void arg) {
		visitChildren(ast, null);
		return null;
	}

	@Override
	public Void methodDecl(MethodDecl ast, Void arg) {
		// Check that methods overridden from a parent class agree
		// on number/type of parameters
		MethodSymbol sym = ast.sym;
		if (sym.getOverriddenMethod().isPresent()) {
			MethodSymbol superSym = sym.getOverriddenMethod().get();
			if (superSym.getParameters().size() != sym.getParameters().size()) {
				throw new SemanticFailure(Cause.INVALID_OVERRIDE,
						"Overridden method %s has %d parameters, "
								+ "but original has %d", ast.name, sym
								.getParameters().size(), superSym
								.getParameters().size());
			}
		}
		return null;
	}

}
