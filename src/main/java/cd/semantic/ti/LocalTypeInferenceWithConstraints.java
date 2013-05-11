package cd.semantic.ti;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import cd.CompilationContext;
import cd.exceptions.SemanticFailure;
import cd.exceptions.SemanticFailure.Cause;
import cd.ir.AstVisitor;
import cd.ir.ExprVisitor;
import cd.ir.ast.Assign;
import cd.ir.ast.ClassDecl;
import cd.ir.ast.Expr;
import cd.ir.ast.IntConst;
import cd.ir.ast.MethodDecl;
import cd.ir.ast.ReturnStmt;
import cd.ir.ast.Var;
import cd.ir.symbols.MethodSymbol;
import cd.ir.symbols.TypeSymbol;
import cd.ir.symbols.VariableSymbol;
import cd.ir.symbols.VariableSymbol.Kind;
import cd.semantic.ExprTypingVisitor;
import cd.semantic.SymbolTable;
import cd.semantic.TypeSymbolTable;
import cd.semantic.ti.constraintSolving.ConstantTypeSet;
import cd.semantic.ti.constraintSolving.ConstraintSolver;
import cd.semantic.ti.constraintSolving.ConstraintSystem;
import cd.semantic.ti.constraintSolving.TypeVariable;

public class LocalTypeInferenceWithConstraints implements TypeInference {

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
		// Run the expression typing visitor over the method once,
		// such that type symbols are set to something non-null.
		final SymbolTable<VariableSymbol> scope = mdecl.sym.getScope();
		final ExprTypingVisitor exprTypingVisitor = new ExprTypingVisitor(
				typeSymbols);
		mdecl.accept(new AstVisitor<Void, Void>() {

			@Override
			protected Void dfltExpr(Expr expr, Void arg) {
				exprTypingVisitor.type(expr, scope);
				return null;
			}

		}, null);
		
		ConstraintGenerator constraintGen = new ConstraintGenerator(mdecl,
				typeSymbols);
		constraintGen.generate();
		ConstraintSolver solver = new ConstraintSolver(
				constraintGen.getConstraintSystem());
		solver.solve();
		if (!solver.hasSolution()) {
			throw new SemanticFailure(Cause.TYPE_ERROR,
					"Type inference was unable to resolve type constraints");
		} else {
			for (VariableSymbol varSym : mdecl.sym.getLocals()) {
				Set<TypeSymbol> possibleTypes = constraintGen
						.getPossibleTypes(varSym);
				if (possibleTypes.isEmpty()) {
					throw new SemanticFailure(Cause.TYPE_ERROR,
							"No type could be found for " + varSym.name);
				} else if (possibleTypes.size() > 1) {
					throw new SemanticFailure(Cause.TYPE_ERROR,
							"Type inference resulted in ambiguous type for "
									+ varSym.name);
				} else {
					varSym.setType(possibleTypes.iterator().next());
				}
			}
		}

	}

	/**
	 * ConstraintGenerator is responsible for creating as many type variables
	 * and constraints as necessary for a method.
	 */
	public class ConstraintGenerator extends AstVisitor<TypeVariable, Void> {
		private MethodDecl mdecl;
		private ConstraintSystem constraintSystem = new ConstraintSystem();
		private TypeVariable returnTypeVariable;
		private final TypeSymbolTable typeSymbols;

		// Map to remember the type variables for our parameters and locals,
		// i.e. what we are eventually interested in.
		// Note to avoid confusion: VariableSymbols are symbols for program
		// variables while
		// these TypeVariables are constraint solver variables describing the
		// type of such program variables
		private final Map<VariableSymbol, TypeVariable> localSymbolVariables = new HashMap<>();

		public ConstraintGenerator(MethodDecl mdecl, TypeSymbolTable typeSymbols) {
			this.mdecl = mdecl;
			this.typeSymbols = typeSymbols;
		}

		public ConstraintSystem getConstraintSystem() {
			return constraintSystem;
		}

		public Set<TypeSymbol> getPossibleTypes(VariableSymbol varSym) {
			return localSymbolVariables.get(varSym).getTypes();
		}

		public void generate() {
			// variables and constraints for parameters (types given!)
			MethodSymbol msym = mdecl.sym;
			for (VariableSymbol varSym : msym.getParameters()) {
				TypeVariable typeVar = constraintSystem.addTypeVariable();
				ConstantTypeSet typeConst = new ConstantTypeSet(
						varSym.getType());
				constraintSystem.addConstEquality(typeVar, typeConst);
				localSymbolVariables.put(varSym, typeVar);
			}

			// type variables for local variables
			for (VariableSymbol varSym : msym.getLocals()) {
				TypeVariable typeVar = constraintSystem.addTypeVariable();
				localSymbolVariables.put(varSym, typeVar);
			}

			// type variable and constraints for return value (if any)
			if (msym.returnType != typeSymbols.getVoidType()) {
				returnTypeVariable = constraintSystem.addTypeVariable();
				ConstantTypeSet typeConst = new ConstantTypeSet(msym.returnType);
				constraintSystem
						.addConstEquality(returnTypeVariable, typeConst);
			}

			ConstraintStmtVisitor constraintVisitor = new ConstraintStmtVisitor();
			mdecl.accept(constraintVisitor, null);
		}

		public class ConstraintStmtVisitor extends AstVisitor<Void, Void> {
			ConstraintExprVisitor exprVisitor = new ConstraintExprVisitor();

			@Override
			public Void returnStmt(ReturnStmt ast, Void arg) {
				if (ast.arg() != null) {
					TypeVariable exprType = exprVisitor.visit(ast.arg(), null);
					constraintSystem.addVarEquality(exprType,
							returnTypeVariable);
				}
				return null;
			}

			@Override
			public Void assign(Assign assign, Void arg) {
				Expr lhs = assign.left();
				if (lhs instanceof Var) {
					VariableSymbol varSym = ((Var) lhs).getSymbol();
					if (varSym.getKind().equals(Kind.LOCAL)) {
						TypeVariable exprTypeVar = exprVisitor.visit(
								assign.right(), null);
						TypeVariable localTypeVar = localSymbolVariables
								.get(varSym);
						constraintSystem.addVarInequality(exprTypeVar,
								localTypeVar);
					}
				}
				return null;

			}

			// TODO: other statements which are necessary
		}

		public class ConstraintExprVisitor extends
				ExprVisitor<TypeVariable, Void> {
			@Override
			public TypeVariable var(Var ast, Void arg) {
				VariableSymbol varSym = ast.getSymbol();
				return localSymbolVariables.get(varSym);
			}

			@Override
			public TypeVariable intConst(IntConst ast, Void arg) {
				TypeVariable typeVar = constraintSystem.addTypeVariable();
				ConstantTypeSet intTypeSet = new ConstantTypeSet(
						typeSymbols.getIntType());
				constraintSystem.addConstEquality(typeVar, intTypeSet);
				return typeVar;
			}

			// TODO: all expression cases
		}
	}

}
