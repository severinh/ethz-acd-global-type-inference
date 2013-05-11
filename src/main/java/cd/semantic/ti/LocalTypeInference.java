package cd.semantic.ti;

import cd.CompilationContext;
import cd.ir.ast.ClassDecl;
import cd.ir.ast.MethodDecl;
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
		TypeSymbolTable typeSymbols = context.getTypeSymbols();
		for (ClassDecl classDecl : context.getAstRoots()) {
			for (MethodDecl methodDecl : classDecl.methods()) {
				inferTypes(methodDecl, typeSymbols);
			}
		}
	}

	/**
	 * Infer types of all local variable symbols in the given method, based on
	 * the given type symbol table.
	 */
	public abstract void inferTypes(MethodDecl mdecl,
			TypeSymbolTable typeSymbols);

}