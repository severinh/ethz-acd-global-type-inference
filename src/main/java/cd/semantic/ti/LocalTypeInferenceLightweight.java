package cd.semantic.ti;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

import cd.exceptions.SemanticFailure;
import cd.exceptions.SemanticFailure.Cause;
import cd.ir.AstVisitor;
import cd.ir.ast.Assign;
import cd.ir.ast.Expr;
import cd.ir.ast.MethodDecl;
import cd.ir.ast.Var;
import cd.ir.symbols.TypeSymbol;
import cd.ir.symbols.VariableSymbol;
import cd.ir.symbols.VariableSymbol.Kind;
import cd.semantic.ExprTypingVisitor;
import cd.semantic.SymbolTable;
import cd.semantic.TypeSymbolTable;

public class LocalTypeInferenceLightweight extends LocalTypeInference {

	@Override
	public void inferTypes(MethodDecl mdecl, TypeSymbolTable typeSymbols) {
		SymbolTable<VariableSymbol> scope = mdecl.sym.getScope();
		ExprTypingVisitor exprTypingVisitor = new ExprTypingVisitor(typeSymbols);

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
			if (lca.equals(typeSymbols.getTopType())) {
				throw new SemanticFailure(Cause.TYPE_ERROR,
						"Variable %s cannot have top type.", variable);
			}
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
		private final Multimap<VariableSymbol, Assign> variableUseMap;

		public AssignDeps() {
			assigns = new LinkedHashSet<>();
			variableUseMap = LinkedHashMultimap.create();
		}

		public void registerAssign(Assign assign) {
			assigns.add(assign);
		}

		public Set<Assign> getAssigns() {
			return Collections.unmodifiableSet(assigns);
		}

		public void registerVariableUse(VariableSymbol variable,
				Assign usingAssign) {
			variableUseMap.put(variable, usingAssign);
		}

		public Collection<Assign> getVariableUses(VariableSymbol variable) {
			return variableUseMap.get(variable);
		}

		@Override
		public String toString() {
			return "AssignDeps [assigns=" + assigns + " variableUseMap="
					+ variableUseMap + "]";
		}

	}

}
