package cd.semantic.ti;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import cd.exceptions.SemanticFailure;
import cd.exceptions.SemanticFailure.Cause;
import cd.ir.AstVisitor;
import cd.ir.ast.Assign;
import cd.ir.ast.BuiltInWrite;
import cd.ir.ast.BuiltInWriteFloat;
import cd.ir.ast.IfElse;
import cd.ir.ast.MethodCall;
import cd.ir.ast.MethodDecl;
import cd.ir.ast.ReturnStmt;
import cd.ir.ast.WhileLoop;
import cd.ir.symbols.ClassSymbol;
import cd.ir.symbols.MethodSymbol;
import cd.ir.symbols.TypeSymbol;
import cd.ir.symbols.VariableSymbol;
import cd.semantic.TypeSymbolTable;
import cd.semantic.ti.constraintSolving.ConstantTypeSet;
import cd.semantic.ti.constraintSolving.ConstantTypeSetFactory;
import cd.semantic.ti.constraintSolving.ConstraintSolver;
import cd.semantic.ti.constraintSolving.ConstraintSystem;
import cd.semantic.ti.constraintSolving.TypeSet;
import cd.semantic.ti.constraintSolving.TypeVariable;

import com.google.common.base.Optional;

public class LocalTypeInferenceWithConstraints extends LocalTypeInference {

	@Override
	public void inferTypes(MethodDecl mdecl, TypeSymbolTable typeSymbols) {
		ConstraintGenerator generator = new ConstraintGenerator(mdecl,
				typeSymbols);
		generator.generate();
		ConstraintSystem constraintSystem = generator.getConstraintSystem();
		ConstraintSolver solver = new ConstraintSolver(constraintSystem);
		solver.solve();
		if (!solver.hasSolution()) {
			throw new SemanticFailure(Cause.TYPE_INFERENCE_ERROR,
					"Type inference was unable to resolve type constraints");
		} else {
			for (VariableSymbol varSym : mdecl.sym.getLocals()) {
				Set<TypeSymbol> possibleTypes = generator
						.getPossibleTypes(varSym);
				TypeSymbol type = null;
				if (possibleTypes.isEmpty()) {
					// Use the bottom type if there are no types in the type
					// set. Since the constraint system has been solved
					// successfully, this usually (always?) means that the
					// variable symbol is not used at all.
					type = typeSymbols.getBottomType();
				} else if (possibleTypes.size() == 1) {
					type = possibleTypes.iterator().next();
				} else if (possibleTypes.size() > 1) {
					// NOTE: we may still try to take the join (lca). This is
					// sometimes necessary.
					TypeSymbol[] typesArray = possibleTypes
							.toArray(new TypeSymbol[possibleTypes.size()]);
					TypeSymbol lca = typeSymbols.getLCA(typesArray);
					if (lca != typeSymbols.getTopType()) {
						type = lca;
					} else {
						throw new SemanticFailure(Cause.TYPE_INFERENCE_ERROR,
								"Type inference resulted in ambiguous type for "
										+ varSym.name);
					}
				}
				varSym.setType(type);
			}
		}

	}

	/**
	 * ConstraintGenerator is responsible for creating as many type variables
	 * and constraints as necessary for a method.
	 */
	public class ConstraintGenerator extends AstVisitor<TypeVariable, Void>
			implements ConstraintGenerationContext {
		private final TypeSymbolTable typeSymbols;
		private final MethodDecl mdecl;
		private final ConstraintSystem constraintSystem;

		private final MethodSymbolCache methodSymbolCache;
		private final ClassSymbolFieldCache classFieldSymbolCache;
		private final ConstantTypeSetFactory constantTypeSetFactory;

		private ConstantTypeSet allowedReturnTypeSet;

		// Map to remember the type variables for our parameters and locals,
		// i.e. what we are eventually interested in.
		// Note to avoid confusion: VariableSymbols are symbols for program
		// variables while
		// these TypeVariables are constraint solver variables describing the
		// type of such program variables
		private final Map<VariableSymbol, TypeSet> localSymbolVariables = new HashMap<>();

		private final ExprConstraintGenerator exprVisitor = new ExprConstraintGenerator(
				this);

		public ConstraintGenerator(MethodDecl mdecl, TypeSymbolTable typeSymbols) {
			this.typeSymbols = typeSymbols;
			this.mdecl = mdecl;
			this.constraintSystem = new ConstraintSystem();
			this.methodSymbolCache = MethodSymbolCache.of(typeSymbols);
			this.classFieldSymbolCache = ClassSymbolFieldCache.of(typeSymbols);
			this.constantTypeSetFactory = new ConstantTypeSetFactory(
					typeSymbols);
		}

		@Override
		public ConstraintSystem getConstraintSystem() {
			return constraintSystem;
		}

		@Override
		public ConstantTypeSetFactory getConstantTypeSetFactory() {
			return constantTypeSetFactory;
		}

		@Override
		public Collection<MethodSymbol> getMatchingMethods(String name,
				int parameterCount) {
			return methodSymbolCache.get(name, parameterCount);
		}

		@Override
		public Collection<ClassSymbol> getClassesDeclaringField(String fieldName) {
			return classFieldSymbolCache.get(fieldName);
		}

		@Override
		public TypeSet getLocalVariableTypeSet(VariableSymbol localVariable) {
			return localSymbolVariables.get(localVariable);
		}

		@Override
		public MethodSymbol getCurrentMethod() {
			return mdecl.sym;
		}

		@Override
		public TypeSymbolTable getTypeSymbolTable() {
			return typeSymbols;
		}

		public Set<TypeSymbol> getPossibleTypes(VariableSymbol varSym) {
			return localSymbolVariables.get(varSym).getTypes();
		}

		public void generate() {
			// variables and constraints for parameters (types given!)
			MethodSymbol msym = mdecl.sym;
			for (VariableSymbol varSym : msym.getParameters()) {
				// Do not create a type variable for the parameter.
				// There is nothing to infer since the parameter type is fixed.
				// However, it is not correct to use a singleton type set
				// with only the declared type, because otherwise, assigning
				// a valid value of a subtype would not be possible.
				// Thus, use the constant set of all declarable subtypes.
				ConstantTypeSet typeConst = constantTypeSetFactory
						.makeDeclarableSubtypes(varSym.getType());
				localSymbolVariables.put(varSym, typeConst);
			}

			// type variables for local variables
			for (VariableSymbol varSym : msym.getLocals()) {
				TypeVariable typeVar = constraintSystem
						.addTypeVariable("local_" + varSym.name);
				localSymbolVariables.put(varSym, typeVar);
			}

			// type variable and constraints for return value (if any)
			if (msym.returnType == typeSymbols.getVoidType()) {
				allowedReturnTypeSet = constantTypeSetFactory.makeEmpty();
			} else {
				allowedReturnTypeSet = constantTypeSetFactory
						.makeDeclarableSubtypes(msym.returnType);
			}

			ConstraintStmtVisitor constraintVisitor = new ConstraintStmtVisitor();
			mdecl.accept(constraintVisitor, null);
		}

		public class ConstraintStmtVisitor extends AstVisitor<Void, Void> {

			@Override
			public Void returnStmt(ReturnStmt ast, Void arg) {
				if (ast.arg() != null) {
					TypeSet exprTypeSet = exprVisitor.visit(ast.arg());
					constraintSystem.addUpperBound(exprTypeSet,
							allowedReturnTypeSet);
				}
				return null;
			}

			@Override
			public Void assign(Assign assign, Void arg) {
				TypeSet lhsTypeSet = exprVisitor.visit(assign.left());
				TypeSet exprTypeSet = exprVisitor.visit(assign.right());
				constraintSystem.addInequality(exprTypeSet, lhsTypeSet);
				return null;
			}

			@Override
			public Void builtInWrite(BuiltInWrite ast, Void arg) {
				TypeSet argTypeSet = exprVisitor.visit(ast.arg());
				ConstantTypeSet intTypeSet = constantTypeSetFactory.makeInt();
				constraintSystem.addEquality(argTypeSet, intTypeSet);
				return null;
			}

			@Override
			public Void builtInWriteFloat(BuiltInWriteFloat ast, Void arg) {
				TypeSet argTypeSet = exprVisitor.visit(ast.arg());
				ConstantTypeSet floatTypeSet = constantTypeSetFactory
						.makeFloat();
				constraintSystem.addEquality(argTypeSet, floatTypeSet);
				return null;
			}

			@Override
			public Void methodCall(MethodCall call, Void arg) {
				exprVisitor.createMethodCallConstraints(call.methodName,
						call.receiver(), call.argumentsWithoutReceiver(),
						Optional.<TypeVariable> absent());
				return null;
			}

			@Override
			public Void ifElse(IfElse ast, Void arg) {
				visit(ast.then(), null);
				visit(ast.otherwise(), null);
				TypeSet ifExprTypeSet = exprVisitor.visit(ast.condition(), arg);
				TypeSet booleanType = constantTypeSetFactory.makeBoolean();
				constraintSystem.addEquality(ifExprTypeSet, booleanType);
				return null;
			}

			@Override
			public Void whileLoop(WhileLoop ast, Void arg) {
				visit(ast.body(), null);
				TypeSet whileConditionExprTypeSet = exprVisitor.visit(
						ast.condition(), arg);
				TypeSet booleanType = constantTypeSetFactory.makeBoolean();
				constraintSystem.addEquality(whileConditionExprTypeSet,
						booleanType);
				return null;
			}
		}

	}

}
