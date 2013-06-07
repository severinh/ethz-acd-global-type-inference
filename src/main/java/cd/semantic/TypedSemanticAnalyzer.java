package cd.semantic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cd.CompilationContext;
import cd.exceptions.SemanticFailure;
import cd.exceptions.SemanticFailure.Cause;
import cd.ir.AstVisitor;
import cd.ir.ast.ClassDecl;
import cd.ir.ast.MethodCall;
import cd.ir.ast.MethodCallExpr;
import cd.ir.ast.MethodDecl;
import cd.ir.symbols.ClassSymbol;
import cd.ir.symbols.MethodSymbol;
import cd.ir.symbols.Symbol;

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
		checkStartPoint(context.getTypeSymbols());
		checkUsedMethodBodies(classDecls);
		rewriteMethodBodies(classDecls);
	}

	/**
	 * Transitively check which methods are used (i.e. reachable) from main,
	 * and perform semantic checks on them.
	 */
	private void checkUsedMethodBodies(List<ClassDecl> classDecls) {
		MethodSymbol main = context.getMainType().getMethod("main");
		ReachableMethodChecker reachableMethodChecker = new ReachableMethodChecker();
		reachableMethodChecker.checkReachableFrom(context.getMethodDecl(main));
		reachableMethodChecker.removeUnusedMethods();
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
			if (mainMethod.getParameters().isEmpty()
					&& mainMethod.returnType == context.getTypeSymbols()
							.getVoidType()) {
				context.setMainType(cs);
				return; // found the main() method!
			}
		}
		throw new SemanticFailure(Cause.INVALID_START_POINT);
	}


	private void checkMethodBody(MethodDecl md) {
		TypeSymbolTable typeSyms = context.getTypeSymbols();
		TypeChecker tc = new TypeChecker(typeSyms);

		boolean hasReturn = new ReturnCheckerVisitor().visit(md.body(),
				null);

		if (md.sym.returnType != typeSyms.getVoidType()
				&& !hasReturn) {
			throw new SemanticFailure(Cause.MISSING_RETURN,
					"Method %s.%s is missing a return statement",
					md.sym.getOwner().name, md.name);
		}

		tc.checkStmt(md, md.sym.getScope());
	}

	private void rewriteMethodBodies(List<ClassDecl> classDecls) {
		for (ClassDecl cd : classDecls)
			new ExprRewriter().visit(cd, null);
	}
	
	
	/**
	 * Check all method bodies reachable from a given method.
	 * A method is reachable if there is a call from another reachable method to it.
	 *
	 */
	private final class ReachableMethodChecker extends AstVisitor<Void, Void> {
		private final Set<MethodSymbol> usedMethods;
	
		private ReachableMethodChecker() {
			this.usedMethods = new HashSet<>();
		}
		
		public void checkReachableFrom(MethodDecl ast) {
			visit(ast, null);
		}
	
		@Override
		public Void methodDecl(MethodDecl ast, Void arg) {
			checkMethodBody(ast);
			usedMethods.add(ast.sym);
			return super.methodDecl(ast, null);
		}
	
		@Override
		public Void methodCall(MethodCall ast, Void arg) {
			handleCallSite(ast.sym);
			return null;
		}
	
		@Override
		public Void methodCall(MethodCallExpr ast, Void arg) {
			handleCallSite(ast.sym);
			return null;
		}
	
		private void handleCallSite(MethodSymbol msym) {
			// When a method symbol is used, check its method declaration 
			// and all the overriding methods as well. This is an approximation but necessary.
			List<MethodSymbol> toVisit = new ArrayList<>();
			toVisit.add(msym);
			toVisit.addAll(msym.getOverriddenBy());
			for (MethodSymbol m : toVisit) {
				if (!usedMethods.contains(m)) {
					visit(context.getMethodDecl(m), null);
				}
			}
		}
		
		/**
		 * Removes all the unused methods. This prevents the code generator
		 * from processing methods we could not correctly type but that are never used.
		 * The reason we cannot type them is often exactly because they are never called.
		 */
		public void removeUnusedMethods() {
			for (ClassDecl cdecl : context.getAstRoots()) {
				ClassSymbol csym = cdecl.sym;
				Set<MethodSymbol> unusedMethods = new HashSet<>(csym.getDeclaredMethods());
				unusedMethods.removeAll(usedMethods);
				for (MethodSymbol unusedMethodSym : unusedMethods) {
					csym.removeMethod(unusedMethodSym);
					cdecl.removeChild(context.getMethodDecl(unusedMethodSym));
				}
			}
		}
	}

}
