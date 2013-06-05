package cd.semantic.ti;

import java.util.List;
import java.util.Set;

import cd.ir.ast.Ast;
import cd.ir.ast.Expr;
import cd.ir.symbols.TypeSymbol;
import cd.semantic.ti.constraintSolving.ConstantTypeSet;
import cd.semantic.ti.constraintSolving.TypeSet;
import cd.semantic.ti.constraintSolving.TypeVariable;
import cd.semantic.ti.constraintSolving.constraints.ConstraintCondition;

public abstract class ReceiverConstraintGenerator<A extends Ast, RT extends TypeSymbol> {

	protected final ExprConstraintGenerator generator;
	protected final A ast;

	public ReceiverConstraintGenerator(
			ExprConstraintGenerator exprConstraintGenerator, A ast) {
		this.generator = exprConstraintGenerator;
		this.ast = ast;
	}

	public TypeVariable generate() {
		Expr receiver = getReceiver();
		List<? extends Expr> arguments = getArguments();

		TypeSet receiverTypeSet = generator.visit(receiver);
		TypeVariable resultTypeSet = generator.getSystem().addTypeVariable();

		Set<? extends RT> possibleReceiverTypes = getPossibleReceiverTypes();

		for (RT possibleReceiverType : possibleReceiverTypes) {
			ConstraintCondition condition = new ConstraintCondition(
					possibleReceiverType, receiverTypeSet);
			List<? extends TypeSet> parameterTypeSets = getParameterTypeSets(possibleReceiverType);

			for (int i = 0; i < arguments.size(); i++) {
				Expr argument = arguments.get(i);
				TypeSet argumentTypeSet = generator.visit(argument);
				TypeSet parameterTypeSet = parameterTypeSets.get(i);
				generator.getSystem().addInequality(argumentTypeSet,
						parameterTypeSet, condition);
			}

			TypeSet possibleResultTypeSet = getResultTypeSet(possibleReceiverType);
			generator.getSystem().addEquality(resultTypeSet,
					possibleResultTypeSet, condition);
		}

		ConstantTypeSet possibleReceiverTypeSet = new ConstantTypeSet(
				possibleReceiverTypes);
		generator.getSystem().addUpperBound(receiverTypeSet,
				possibleReceiverTypeSet);

		return resultTypeSet;
	}

	protected abstract Expr getReceiver();

	protected abstract List<? extends Expr> getArguments();

	protected abstract Set<? extends RT> getPossibleReceiverTypes();

	protected abstract List<? extends TypeSet> getParameterTypeSets(
			RT receiverType);

	protected abstract TypeSet getResultTypeSet(RT receiverType);

}
