package cd.semantic;

import cd.exceptions.SemanticFailure;
import cd.exceptions.SemanticFailure.Cause;
import cd.ir.ast.ClassDecl;
import cd.ir.ast.MethodDecl;
import cd.ir.symbols.ClassSymbol;
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

	private ClassSymbol classSym;

	public void check(ClassDecl classDecl) {
		visit(classDecl, null);
	}

	@Override
	public Void classDecl(ClassDecl ast, Void arg) {
		classSym = ast.sym;
		visitChildren(ast, null);
		return null;
	}

	@Override
	public Void methodDecl(MethodDecl ast, Void arg) {
		// check that methods overridden from a parent class agree
		// on number/type of parameters
		MethodSymbol sym = ast.sym;
		MethodSymbol superSym = classSym.getSuperClass().getMethod(ast.name);
		sym.overrides = superSym;
		if (superSym != null) {
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
