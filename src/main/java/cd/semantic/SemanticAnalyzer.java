package cd.semantic;

import java.util.ArrayList;
import java.util.List;

import cd.Main;
import cd.exceptions.SemanticFailure;
import cd.exceptions.SemanticFailure.Cause;
import cd.ir.ast.ClassDecl;
import cd.ir.ast.MethodDecl;
import cd.ir.symbols.ArrayTypeSymbol;
import cd.ir.symbols.ClassSymbol;
import cd.ir.symbols.MethodSymbol;
import cd.ir.symbols.Symbol;
import cd.ir.symbols.TypeSymbol;
import cd.ir.symbols.VariableSymbol;

public class SemanticAnalyzer {

	private final Main main;

	public SemanticAnalyzer(Main main) {
		this.main = main;
	}

	public void check(List<ClassDecl> classDecls) throws SemanticFailure {
		SymbolTable<TypeSymbol> typeSymbols = createSymbols(classDecls);
		checkInheritance(classDecls);
		checkStartPoint(typeSymbols);
		checkMethodBodies(typeSymbols, classDecls);
		rewriteMethodBodies(classDecls);
		main.allTypeSymbols = typeSymbols.allSymbols();
	}

	/**
	 * Creates a symbol table with symbols for all built-in types, as well as
	 * all classes and their fields and methods. Also creates a corresponding
	 * array symbol for every type (named {@code type[]}).
	 * 
	 * @see SymbolCreator
	 */
	private SymbolTable<TypeSymbol> createSymbols(List<ClassDecl> classDecls) {

		// Start by creating a symbol for all built-in types.
		SymbolTable<TypeSymbol> typeSymbols = new SymbolTable<>(null);

		typeSymbols.add(main.intType);
		typeSymbols.add(main.booleanType);
		typeSymbols.add(main.floatType);
		typeSymbols.add(main.voidType);
		typeSymbols.add(main.objectType);

		// Add symbols for all declared classes.
		for (ClassDecl ast : classDecls) {
			// Check for classes named Object
			if (ast.name.equals(main.objectType.name))
				throw new SemanticFailure(Cause.OBJECT_CLASS_DEFINED);
			ast.sym = new ClassSymbol(ast);
			typeSymbols.add(ast.sym);
		}

		// Create symbols for arrays of each type.
		for (Symbol sym : new ArrayList<Symbol>(typeSymbols.localSymbols())) {
			ArrayTypeSymbol array = new ArrayTypeSymbol((TypeSymbol) sym);
			typeSymbols.add(array);
		}

		// For each class, create symbols for each method and field
		SymbolCreator sc = new SymbolCreator(main, typeSymbols);
		for (ClassDecl ast : classDecls)
			sc.createSymbols(ast);

		return typeSymbols;
	}

	/**
	 * Check for errors related to inheritance: circular inheritance, invalid
	 * super classes, methods with different types, etc. Note that this must be
	 * run early because other code assumes that the inheritance is correct, for
	 * type checking etc.
	 * 
	 * @see InheritanceChecker
	 */
	private void checkInheritance(List<ClassDecl> classDecls) {
		for (ClassDecl cd : classDecls)
			new InheritanceChecker().visit(cd, null);
	}

	/**
	 * Guarantee there is a class Main which defines a method main with no
	 * arguments.
	 */
	private void checkStartPoint(SymbolTable<TypeSymbol> typeSymbols) {
		Symbol mainClass = typeSymbols.get("Main");
		if (mainClass != null && mainClass instanceof ClassSymbol) {
			ClassSymbol cs = (ClassSymbol) mainClass;
			MethodSymbol mainMethod = cs.getMethod("main");
			if (mainMethod != null && mainMethod.parameters.size() == 0
					&& mainMethod.returnType == main.voidType) {
				main.mainType = cs;
				return; // found the main() method!
			}
		}
		throw new SemanticFailure(Cause.INVALID_START_POINT);
	}

	/**
	 * Check the bodies of methods for errors, particularly type errors but also
	 * undefined identifiers and the like.
	 * 
	 * @see TypeChecker
	 */
	private void checkMethodBodies(SymbolTable<TypeSymbol> typeSymbols,
			List<ClassDecl> classDecls) {
		TypeChecker tc = new TypeChecker(main, typeSymbols);

		for (ClassDecl classd : classDecls) {

			SymbolTable<VariableSymbol> fldTable = new SymbolTable<>(null);

			// add all fields of this class, or any of its super classes
			for (ClassSymbol p = classd.sym; p != null; p = p.superClass)
				for (VariableSymbol s : p.fields.values())
					if (!fldTable.contains(s.name))
						fldTable.add(s);

			// type check any method bodies and final locals
			for (MethodDecl md : classd.methods()) {

				boolean hasReturn = new ReturnCheckerVisitor().visit(md.body(),
						null);

				if (!md.returnType.equals("void") && !hasReturn) {

					throw new SemanticFailure(Cause.MISSING_RETURN,
							"Method %s.%s is missing a return statement",
							classd.name, md.name);

				}

				SymbolTable<VariableSymbol> mthdTable = new SymbolTable<>(
						fldTable);

				mthdTable.add(classd.sym.thisSymbol);

				for (VariableSymbol p : md.sym.parameters) {
					mthdTable.add(p);
				}

				for (VariableSymbol l : md.sym.locals.values()) {
					mthdTable.add(l);
				}

				tc.checkStmt(md, mthdTable);

			}
		}
	}

	private void rewriteMethodBodies(List<ClassDecl> classDecls) {
		for (ClassDecl cd : classDecls)
			new ExprRewriter().visit(cd, null);
	}

}
