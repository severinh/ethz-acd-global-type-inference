package cd.semantic.ti.palsberg.generators;

import java.util.List;

import cd.ir.ast.Expr;
import cd.ir.ast.MethodCallExpr;

/**
 * Generates conditional constraints for method call expressions.
 * 
 * @see ReceiverConstraintGenerator
 */
public class MethodCallExprConstraintGenerator extends
		MethodCallConstraintGeneratorBase<MethodCallExpr> {

	public MethodCallExprConstraintGenerator(
			ExprConstraintGenerator exprConstraintGenerator,
			MethodCallExpr methodCallExpr) {
		super(exprConstraintGenerator, methodCallExpr);
	}

	@Override
	protected Expr getReceiver() {
		return ast.receiver();
	}

	@Override
	protected String getMethodName() {
		return ast.methodName;
	}

	@Override
	protected List<? extends Expr> getArguments() {
		return ast.argumentsWithoutReceiver();
	}

}
