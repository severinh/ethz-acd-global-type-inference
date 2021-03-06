package cd.semantic.ti.palsberg.generators;

import java.util.List;

import cd.ir.ast.Expr;
import cd.ir.ast.MethodCall;

/**
 * Generates conditional constraints for method call statements.
 * 
 * @see ReceiverConstraintGenerator
 */
public class MethodCallConstraintGenerator extends
		MethodCallConstraintGeneratorBase<MethodCall> {

	public MethodCallConstraintGenerator(
			ExprConstraintGenerator exprConstraintGenerator,
			MethodCall methodCall) {
		super(exprConstraintGenerator, methodCall);
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
