package cd.semantic;

import java.util.List;

import cd.CompilationContext;
import cd.exceptions.SemanticFailure;
import cd.exceptions.SemanticFailure.Cause;
import cd.ir.ast.ClassDecl;
import cd.ir.ast.MethodDecl;
import cd.ir.symbols.ClassSymbol;
import cd.ir.symbols.MethodSymbol;
import cd.ir.symbols.Symbol;
import cd.ir.symbols.VariableSymbol;

/**
 * Performs semantic checks on a fully typed AST, based on the already
 * constructed symbol table.
 * 
 * In particular, it checks parameter and return type invariance of overriding
 * methods, extracts the program entry point, and computes the type of each
 * expression in the code. Since expressions have been typed using type
 * inference already at this stage, the types probably do not change anymore.
 * Still, it may not be a useless operation, because it is another safety net in
 * case there is a bug in the type inference.
 */
public class TypedSemanticAnalyzer {

	private final CompilationContext context;

	public TypedSemanticAnalyzer(CompilationContext context) {
		this.context = context;
	}

	public void check(List<ClassDecl> classDecls) throws SemanticFailure {
		checkInheritance(classDecls);
		checkStartPoint(context.typeSymbols);
		checkMethodBodies(context.typeSymbols, classDecls);
		rewriteMethodBodies(classDecls);
	}

	/**
	 * Check for errors related to inheritance: circular inheritance, invalid
	 * super classes. Note that this must be run early because other code
	 * assumes that the inheritance is correct, for type checking etc.
	 * 
	 * @see TypedInheritanceChecker
	 */
	private void checkInheritance(List<ClassDecl> classDecls) {
		for (ClassDecl cd : classDecls)
			new TypedInheritanceChecker().visit(cd, null);
	}

	/**
	 * Guarantee there is a class Main which defines a method main with no
	 * arguments.
	 */
	private void checkStartPoint(TypeSymbolTable typeSymbols) {
		Symbol mainClass = typeSymbols.get("Main");
		if (mainClass != null && mainClass instanceof ClassSymbol) {
			ClassSymbol cs = (ClassSymbol) mainClass;
			MethodSymbol mainMethod = cs.getMethod("main");
			if (mainMethod != null
					&& mainMethod.getParameters().isEmpty()
					&& mainMethod.returnType == context.typeSymbols
							.getVoidType()) {
				context.mainType = cs;
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
	private void checkMethodBodies(TypeSymbolTable typeSymbols,
			List<ClassDecl> classDecls) {
		TypeChecker tc = new TypeChecker(typeSymbols);

		for (ClassDecl classd : classDecls) {
			SymbolTable<VariableSymbol> fldTable = new SymbolTable<>();

			// add all fields of this class, or any of its super classes
			for (ClassSymbol p = classd.sym; p != null; p = p.getSuperClass())
				for (VariableSymbol s : p.getDeclaredFields())
					if (!fldTable.contains(s.name))
						fldTable.add(s);

			// type check any method bodies and final locals
			for (MethodDecl md : classd.methods()) {
				boolean hasReturn = new ReturnCheckerVisitor().visit(md.body(),
						null);

				if (md.sym.returnType != typeSymbols.getVoidType()
						&& !hasReturn) {
					throw new SemanticFailure(Cause.MISSING_RETURN,
							"Method %s.%s is missing a return statement",
							classd.name, md.name);
				}

				SymbolTable<VariableSymbol> mthdTable = new SymbolTable<>(
						fldTable);

				mthdTable.add(classd.sym.getThisSymbol());

				for (VariableSymbol p : md.sym.getParameters()) {
					mthdTable.add(p);
				}

				for (VariableSymbol l : md.sym.getLocals()) {
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
