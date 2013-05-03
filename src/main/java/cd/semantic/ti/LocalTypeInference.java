package cd.semantic.ti;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import cd.CompilationContext;
import cd.ir.AstVisitor;
import cd.ir.ast.Assign;
import cd.ir.ast.ClassDecl;
import cd.ir.ast.Expr;
import cd.ir.ast.MethodDecl;
import cd.ir.ast.Var;
import cd.ir.symbols.TypeSymbol;
import cd.ir.symbols.VariableSymbol;
import cd.ir.symbols.VariableSymbol.Kind;
import cd.semantic.ExprTypingVisitor;
import cd.semantic.SymbolTable;
import cd.semantic.TypeSymbolTable;

public class LocalTypeInference implements TypeInference {

	@Override
	public void inferTypes(CompilationContext context) {
		TypeSymbolTable typeSymbols = context.getTypeSymbols();
		for (ClassDecl classDecl : context.getAstRoots()) {
			for (MethodDecl methodDecl : classDecl.methods()) {
				inferTypes(methodDecl, typeSymbols);
			}
		}
	}

	public void inferTypes(MethodDecl mdecl, TypeSymbolTable typeSymbols) {
		final SymbolTable<VariableSymbol> scope = mdecl.sym.getScope();
		final ExprTypingVisitor exprTypingVisitor = new ExprTypingVisitor(
				typeSymbols);

		// Run the expression typing visitor over the method once,
		// such that type symbols are set to something non-null.
		mdecl.accept(new AstVisitor<Void, Void>() {

			@Override
			protected Void dfltExpr(Expr expr, Void arg) {
				exprTypingVisitor.type(expr, scope);
				return null;
			}

		}, null);

		// Collect all assignments to local variables and record what other
		// assignments such an assignment affects.
		AssignDepsCollector collector = AssignDepsCollector.getInstance();
		AssignDeps dependencies = collector.collect(mdecl);

		// 1:1 implementation of the Bellamy local type inference algorithm
		LinkedHashSet<Assign> workList = new LinkedHashSet<>();
		workList.addAll(dependencies.getAssigns());

		while (!workList.isEmpty()) {
			Assign assign = workList.iterator().next();
			workList.remove(assign);
			VariableSymbol variable = ((Var) assign.left()).getSymbol();
			TypeSymbol exprType = exprTypingVisitor.type(assign.right(), scope);
			TypeSymbol lca = typeSymbols.getLCA(variable.getType(), exprType);
			if (lca != variable.getType()) {
				variable.setType(lca);
				workList.addAll(dependencies.getVariableUses(variable));
			}
		}
	}

	private static class AssignDepsCollector extends
			AstVisitor<Void, AssignDeps> {

		private static final AssignDepsCollector INSTANCE = new AssignDepsCollector();

		public static AssignDepsCollector getInstance() {
			return INSTANCE;
		}

		private Assign currentAssign;

		public AssignDeps collect(MethodDecl methodDecl) {
			AssignDeps dependencies = new AssignDeps();
			visit(methodDecl, dependencies);
			return dependencies;
		}

		@Override
		public Void assign(Assign assign, AssignDeps dependencies) {
			Expr lhs = assign.left();
			if (lhs instanceof Var) {
				VariableSymbol variable = ((Var) lhs).getSymbol();
				if (variable.getKind().equals(Kind.LOCAL)) {
					currentAssign = assign;
					// Only visit the right-hand side of the assignment
					dependencies.registerAssign(assign);
					visit(assign.right(), dependencies);
					currentAssign = null;
				}
			}
			return null;
		}

		@Override
		public Void var(Var ast, AssignDeps dependencies) {
			VariableSymbol variable = ast.getSymbol();
			if (currentAssign != null && variable.getKind().equals(Kind.LOCAL)) {
				dependencies.registerVariableUse(variable, currentAssign);
			}
			return super.var(ast, dependencies);
		}
	}

	/**
	 * Collects all assignments to local variables and records for each local
	 * variable in which assignment it is used on the right-hand side.
	 */
	private static class AssignDeps {

		private final Set<Assign> assigns;
		private final Map<VariableSymbol, Set<Assign>> variableUseMap;

		public AssignDeps() {
			assigns = new LinkedHashSet<>();
			variableUseMap = new LinkedHashMap<>();
		}

		public void registerAssign(Assign assign) {
			assigns.add(assign);
		}

		public Set<Assign> getAssigns() {
			return Collections.unmodifiableSet(assigns);
		}

		public void registerVariableUse(VariableSymbol variable,
				Assign usingAssign) {
			Set<Assign> dependencies = variableUseMap.get(variable);
			if (dependencies == null) {
				dependencies = new LinkedHashSet<>();
				variableUseMap.put(variable, dependencies);
			}
			dependencies.add(usingAssign);
		}

		public Set<Assign> getVariableUses(VariableSymbol variable) {
			Set<Assign> result = variableUseMap.get(variable);
			if (result == null) {
				return Collections.emptySet();
			} else {
				return Collections.unmodifiableSet(result);
			}
		}

		@Override
		public String toString() {
			return "AssignDeps [assigns=" + assigns + " variableUseMap="
					+ variableUseMap + "]";
		}

	}

}
