package cd.semantic.ti;

import java.util.HashMap;
import java.util.Map;

import cd.CompilationContext;
import cd.ir.AstVisitor;
import cd.ir.ExprVisitor;
import cd.ir.ast.ClassDecl;
import cd.ir.ast.MethodDecl;
import cd.ir.ast.ReturnStmt;
import cd.ir.ast.Var;
import cd.ir.symbols.MethodSymbol;
import cd.ir.symbols.VariableSymbol;
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
		ConstraintGenerator constraintGen = new ConstraintGenerator(mdecl,
				typeSymbols);
		constraintGen.generate();
		ConstraintSolver solver = new ConstraintSolver(
				constraintGen.getConstraintSystem());
		solver.solve();

		// TODO: annotate the VariableSymbols with the types obtained from the solution
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
		private final Map<VariableSymbol, TypeVariable> localSymbolVariables = new HashMap<VariableSymbol, TypeVariable>();

		public ConstraintGenerator(MethodDecl mdecl, TypeSymbolTable typeSymbols) {
			this.mdecl = mdecl;
			this.typeSymbols = typeSymbols;
		}

		public ConstraintSystem getConstraintSystem() {
			return constraintSystem;
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
			
			// TODO: other statements which are necessary
		}

		public class ConstraintExprVisitor extends
				ExprVisitor<TypeVariable, Void> {
			@Override
			public TypeVariable var(Var ast, Void arg) {
				VariableSymbol varSym = ast.getSymbol();
				return localSymbolVariables.get(varSym);
			}
			
			// TODO: all expression cases
		}
	}

}
