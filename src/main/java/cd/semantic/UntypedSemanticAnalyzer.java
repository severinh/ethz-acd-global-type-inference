package cd.semantic;

import java.util.ArrayList;
import java.util.List;

import cd.CompilationContext;
import cd.exceptions.SemanticFailure;
import cd.exceptions.SemanticFailure.Cause;
import cd.ir.ast.ClassDecl;
import cd.ir.symbols.ArrayTypeSymbol;
import cd.ir.symbols.ClassSymbol;
import cd.ir.symbols.Symbol;
import cd.ir.symbols.TypeSymbol;

/**
 * Creates the symbol table for an AST with potentially missing types and
 * performs as many semantic checks as possible under this constraint.
 * 
 * In particular, it establishes the superclass and method overriding
 * relationships. The basic semantic checks include the detection of circular
 * inheritance and inconsistent parameter counts in overriding methods.
 * 
 * This semantic check is meant to be run early, such that type inference can
 * then rely on certain basic guarantees on the semantic correctness.
 */
public class UntypedSemanticAnalyzer {

	private final CompilationContext context;

	public UntypedSemanticAnalyzer(CompilationContext context) {
		this.context = context;
	}

	public void check(List<ClassDecl> classDecls) throws SemanticFailure {
		context.typeSymbols = createSymbols(classDecls);
		checkUntypedInheritance(classDecls);
	}

	/**
	 * Creates a symbol table with symbols for all built-in types, as well as
	 * all classes and their fields and methods. Also creates a corresponding
	 * array symbol for every type (named {@code type[]}).
	 * 
	 * @see SymbolCreator
	 */
	private TypeSymbolTable createSymbols(List<ClassDecl> classDecls) {
		TypeSymbolTable typeSymbols = new TypeSymbolTable();

		// Add symbols for all declared classes.
		for (ClassDecl ast : classDecls) {
			// Check for classes named Object
			if (ast.name.equals(typeSymbols.getObjectType().name)) {
				throw new SemanticFailure(Cause.OBJECT_CLASS_DEFINED);
			}
			ast.sym = new ClassSymbol(ast);
			typeSymbols.add(ast.sym);
		}

		// Create symbols for arrays of each type.
		for (Symbol sym : new ArrayList<Symbol>(typeSymbols.localSymbols())) {
			ArrayTypeSymbol array = new ArrayTypeSymbol((TypeSymbol) sym);
			typeSymbols.add(array);
		}

		// For each class, create symbols for each method and field
		SymbolCreator sc = new SymbolCreator(typeSymbols);
		for (ClassDecl classDecl : classDecls) {
			sc.createSymbols(classDecl);
		}

		return typeSymbols;
	}

	/**
	 * Check for errors related to inheritance: circular inheritance, invalid
	 * super classes. Note that this must be run early because other code
	 * assumes that the inheritance is correct, for type checking etc.
	 * 
	 * @see UntypedInheritanceChecker
	 */
	private void checkUntypedInheritance(List<ClassDecl> classDecls) {
		UntypedInheritanceChecker inheritanceChecker = new UntypedInheritanceChecker();
		for (ClassDecl classDecl : classDecls) {
			inheritanceChecker.check(classDecl);
		}
	}

}
