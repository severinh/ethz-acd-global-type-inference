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
			for (final MethodDecl mdecl : classDecl.methods()) {
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