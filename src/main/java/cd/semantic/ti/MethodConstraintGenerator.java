package cd.semantic.ti;

import cd.ir.AstVisitor;
import cd.ir.ast.Assign;
import cd.ir.ast.BuiltInWrite;
import cd.ir.ast.BuiltInWriteFloat;
import cd.ir.ast.Expr;
import cd.ir.ast.IfElse;
import cd.ir.ast.MethodCall;
import cd.ir.ast.MethodDecl;
import cd.ir.ast.ReturnStmt;
import cd.ir.ast.WhileLoop;
import cd.ir.symbols.MethodSymbol;
import cd.ir.symbols.VariableSymbol;
import cd.semantic.ti.constraintSolving.ConstantTypeSet;
import cd.semantic.ti.constraintSolving.ConstantTypeSetFactory;
import cd.semantic.ti.constraintSolving.ConstraintSystem;
import cd.semantic.ti.constraintSolving.TypeSet;
import cd.util.Pair;

/**
 * Recursively generates the type constraints for an individual methods
 * 
 * The type sets associated with fields, parameters, local variables and return
 * values are looked up in the context.
 */
public class MethodConstraintGenerator extends AstVisitor<Void, Void> {

	private final MethodDecl methodDecl;
	private final ConstraintGeneratorContext context;
	private final ExprConstraintGenerator exprConstraintGenerator;

	public MethodConstraintGenerator(MethodDecl methodDecl,
			ConstraintGeneratorContext context) {
		this.methodDecl = methodDecl;
		this.context = context;
		this.exprConstraintGenerator = new ExprConstraintGenerator(
				methodDecl.sym, context);
	}

	public void generate() {
		// For overriding methods, generate equality constraints on the
		// parameter and return type variables. In the case of local type
		// inference, the resulting constraints are trivially satisfied.
		MethodSymbol method = methodDecl.sym;
		if (method.getOverriddenMethod().isPresent()) {
			MethodSymbol overriddenMethod = method.getOverriddenMethod().get();
			for (Pair<VariableSymbol> pair : Pair.zip(method.getParameters(),
					overriddenMethod.getParameters())) {
				TypeSet typeSet = context.getVariableTypeSet(pair.a);
				TypeSet overriddenTypeSet = context.getVariableTypeSet(pair.b);
				getSystem().addEquality(typeSet, overriddenTypeSet);
			}

			TypeSet returnTypeSet = context.getReturnTypeSet(method);
			TypeSet overriddenReturnTypeSet = context
					.getReturnTypeSet(overriddenMethod);
			getSystem().addEquality(returnTypeSet, overriddenReturnTypeSet);
		}

		methodDecl.accept(this, null);
	}

	/**
	 * Convenience shortcut for {@code exprConstraintGenerator.visit(expr)}.
	 */
	private TypeSet getExprTypeSet(Expr expr) {
		return exprConstraintGenerator.visit(expr);
	}

	/**
	 * Convenience shortcut for {@code context.getConstantTypeSetFactory()}.
	 */
	private ConstantTypeSetFactory getTypeSetFactory() {
		return context.getConstantTypeSetFactory();
	}

	/**
	 * Convenience shortcut for {@code context.getConstraintSystem()}.
	 */
	private ConstraintSystem getSystem() {
		return context.getConstraintSystem();
	}

	@Override
	public Void returnStmt(ReturnStmt ast, Void arg) {
		if (ast.arg() != null) {
			TypeSet exprTypeSet = getExprTypeSet(ast.arg());
			context.getConstraintSystem().addInequality(exprTypeSet,
					context.getReturnTypeSet(methodDecl.sym));
		}
		return null;
	}

	@Override
	public Void assign(Assign assign, Void arg) {
		TypeSet lhsTypeSet = getExprTypeSet(assign.left());
		TypeSet exprTypeSet = getExprTypeSet(assign.right());
		getSystem().addInequality(exprTypeSet, lhsTypeSet);
		return null;
	}

	@Override
	public Void builtInWrite(BuiltInWrite ast, Void arg) {
		TypeSet argTypeSet = getExprTypeSet(ast.arg());
		ConstantTypeSet intTypeSet = getTypeSetFactory().makeInt();
		getSystem().addEquality(argTypeSet, intTypeSet);
		return null;
	}

	@Override
	public Void builtInWriteFloat(BuiltInWriteFloat ast, Void arg) {
		TypeSet argTypeSet = getExprTypeSet(ast.arg());
		ConstantTypeSet floatTypeSet = getTypeSetFactory().makeFloat();
		getSystem().addEquality(argTypeSet, floatTypeSet);
		return null;
	}

	@Override
	public Void methodCall(MethodCall call, Void arg) {
		new MethodCallConstraintGenerator(exprConstraintGenerator, call)
				.generate();
		return null;
	}

	@Override
	public Void ifElse(IfElse ast, Void arg) {
		visit(ast.then(), null);
		visit(ast.otherwise(), null);

		TypeSet ifExprTypeSet = getExprTypeSet(ast.condition());
		TypeSet booleanType = getTypeSetFactory().makeBoolean();
		getSystem().addEquality(ifExprTypeSet, booleanType);
		return null;
	}

	@Override
	public Void whileLoop(WhileLoop ast, Void arg) {
		visit(ast.body(), null);

		TypeSet whileConditionExprTypeSet = getExprTypeSet(ast.condition());
		TypeSet booleanType = getTypeSetFactory().makeBoolean();
		getSystem().addEquality(whileConditionExprTypeSet, booleanType);
		return null;
	}

}