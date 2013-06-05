package cd.semantic.ti;

import java.util.List;
import cd.ir.ast.Expr;
import cd.ir.ast.MethodCallExpr;

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
