package cd.semantic;

import cd.exceptions.SemanticFailure;
import cd.exceptions.SemanticFailure.Cause;
import cd.ir.ast.ClassDecl;
import cd.ir.ast.MethodDecl;
import cd.ir.symbols.ClassSymbol;
import cd.ir.symbols.MethodSymbol;
import cd.ir.symbols.VariableSymbol;
import cd.ir.AstVisitor;
import cd.util.Pair;

/**
 * Detects inheritance errors in an AST with only typed variables.
 * 
 * In particular, it checks that the parameter and return types of overriding
 * methods match.
 * 
 * @todo find a better class name
 */
public class TypedInheritanceChecker extends AstVisitor<Void, Void> {

	private ClassSymbol classSym;

	@Override
	public Void classDecl(ClassDecl ast, Void arg) {
		classSym = ast.sym;
		return this.visitChildren(ast, null);
	}

	@Override
	public Void methodDecl(MethodDecl ast, Void arg) {
		// check that methods overridden from a parent class agree
		// on number/type of parameters
		MethodSymbol sym = ast.sym;
		MethodSymbol superSym = classSym.superClass.getMethod(ast.name);
		if (superSym != null) {
			for (Pair<VariableSymbol> pair : Pair.zip(sym.parameters,
					superSym.parameters))
				if (pair.a.type != pair.b.type)
					throw new SemanticFailure(
							Cause.INVALID_OVERRIDE,
							"Method parameter %s has type %s, but "
									+ "corresponding base class parameter %s has type %s",
							pair.a.name, pair.a.type, pair.b.name, pair.b.type);
			if (superSym.returnType != sym.returnType)
				throw new SemanticFailure(Cause.INVALID_OVERRIDE,
						"Overridden method %s has return type %s,"
								+ "but its superclass has %s", ast.name,
						sym.returnType, superSym.returnType);
		}

		return null;
	}

}
