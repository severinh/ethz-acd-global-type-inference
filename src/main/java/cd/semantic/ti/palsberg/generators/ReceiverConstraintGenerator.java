package cd.semantic.ti.palsberg.generators;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cd.ir.ast.Ast;
import cd.ir.ast.Expr;
import cd.ir.symbols.TypeSymbol;
import cd.semantic.ti.palsberg.constraints.ConstraintCondition;
import cd.semantic.ti.palsberg.solving.ConstantTypeSet;
import cd.semantic.ti.palsberg.solving.TypeSet;

/**
 * Generic code that generates constraints for any expression that involves a
 * potentially unknown receiver. This includes method calls, field access and
 * array accesses. All of them require very similar conditional constraints.
 * 
 * The class is instantiated for each expression individually.
 * 
 * @param <A>
 *            The expression or statement for which to generate constraints
 * @param <RT>
 *            The kind of type symbols used for receiver types, such as
 *            {@link ClassSymbol} for field accesses and method calls.
 */
public abstract class ReceiverConstraintGenerator<A extends Ast, RT extends TypeSymbol> {

	protected final ExprConstraintGenerator generator;
	protected final A ast;
	protected final TypeSet resultTypeSet;

	public ReceiverConstraintGenerator(
			ExprConstraintGenerator exprConstraintGenerator, A ast) {
		this.generator = exprConstraintGenerator;
		this.ast = ast;
		this.resultTypeSet = generator.getSystem().addTypeVariable();
	}

	public TypeSet generate() {
		TypeSet receiverTypeSet = getReceiverTypeSet();
		List<TypeSet> argumentTypeSets = getArgumentTypeSets();

		Set<? extends RT> possibleReceiverTypes = getPossibleReceiverTypes();
		for (RT possibleReceiverType : possibleReceiverTypes) {
			ConstraintCondition condition = new ConstraintCondition(
					possibleReceiverType, receiverTypeSet);
			List<? extends TypeSet> parameterTypeSets = getParameterTypeSets(possibleReceiverType);

			for (int i = 0; i < argumentTypeSets.size(); i++) {
				TypeSet argumentTypeSet = argumentTypeSets.get(i);
				TypeSet parameterTypeSet = parameterTypeSets.get(i);
				generator.getSystem().addInequality(argumentTypeSet,
						parameterTypeSet, condition);
			}

			TypeSet possibleResultTypeSet = getResultTypeSet(possibleReceiverType);
			generator.getSystem().addEquality(resultTypeSet,
					possibleResultTypeSet, condition);
		}

		// We do not need to generate any constraints for the case that null
		// is in the receiver type set, because null is a subtype of all
		// reference types. Thus, null is not part of the possibleReceiverTypes
		// set. However, we must not disallow null as a receiver. Thus,
		// include it in the upper bound.
		Set<TypeSymbol> possibleReceiverTypesWithNull = new HashSet<>();
		possibleReceiverTypesWithNull.addAll(possibleReceiverTypes);
		possibleReceiverTypesWithNull.add(generator.getTypeSymbols()
				.getNullType());

		ConstantTypeSet possibleReceiverTypeSetWithNull = new ConstantTypeSet(
				possibleReceiverTypesWithNull);
		generator.getSystem().addUpperBound(receiverTypeSet,
				possibleReceiverTypeSetWithNull);

		return resultTypeSet;
	}

	protected abstract Expr getReceiver();

	protected TypeSet getReceiverTypeSet() {
		return generator.visit(getReceiver());
	}

	protected abstract List<? extends Expr> getArguments();

	protected List<TypeSet> getArgumentTypeSets() {
		List<TypeSet> result = new ArrayList<>();
		for (Expr argument : getArguments()) {
			result.add(generator.visit(argument));
		}
		return result;
	}

	protected abstract Set<? extends RT> getPossibleReceiverTypes();

	protected abstract List<? extends TypeSet> getParameterTypeSets(
			RT receiverType);

	protected abstract TypeSet getResultTypeSet(RT receiverType);

}
