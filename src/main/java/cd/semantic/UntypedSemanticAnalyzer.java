package cd.semantic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

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

		// Create the ClassSymbols in the "right" order, meaning that a class is
		// always created before its subclasses. This allows us to pass the
		// superclass to the ClassSymbol constructor. Furthermore, the taken
		// approach allows us to cheaply detect cycles in the subtype hierarchy.

		// Map each class to the list of its subclasses
		Map<String, List<ClassDecl>> subclassMap = new HashMap<>();
		for (ClassDecl classDecl : classDecls) {
			// Check for classes named Object
			if (classDecl.name.equals(typeSymbols.getObjectType().name)) {
				throw new SemanticFailure(Cause.OBJECT_CLASS_DEFINED);
			}

			String superClassName = classDecl.superClass;
			List<ClassDecl> siblings = subclassMap.get(superClassName);
			if (siblings == null) {
				siblings = new ArrayList<>();
				subclassMap.put(superClassName, siblings);
			}
			siblings.add(classDecl);
		}

		// The queue of classes whose symbol has already been created, but not
		// for their subclasses
		Queue<ClassSymbol> workList = new LinkedList<>();
		workList.add(typeSymbols.getObjectType());

		while (!workList.isEmpty()) {
			ClassSymbol superClass = workList.remove();
			List<ClassDecl> subClassDecls = subclassMap.remove(superClass.name);
			if (subClassDecls != null) {
				for (ClassDecl subClassDecl : subClassDecls) {
					ClassSymbol subClass = new ClassSymbol(subClassDecl.name,
							superClass);
					subClassDecl.sym = subClass;
					workList.add(subClass);
					typeSymbols.add(subClass);
				}
			}
		}

		// Check if there are any classes that are not subclasses of Object.
		// This happens if and only if there is a cycle in the hierarchy.
		if (!subclassMap.isEmpty()) {
			ClassDecl classDecl = subclassMap.values().iterator().next().get(0);
			throw new SemanticFailure(Cause.CIRCULAR_INHERITANCE,
					"Class %s is part of a subtyping cycle", classDecl.name);
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
