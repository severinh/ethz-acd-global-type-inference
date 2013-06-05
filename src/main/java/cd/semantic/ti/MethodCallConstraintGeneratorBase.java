package cd.semantic.ti;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cd.ir.ast.Ast;
import cd.ir.symbols.ClassSymbol;
import cd.ir.symbols.MethodSymbol;
import cd.ir.symbols.VariableSymbol;
import cd.semantic.ti.constraintSolving.TypeSet;

public abstract class MethodCallConstraintGeneratorBase<A extends Ast> extends
		ReceiverConstraintGenerator<A, ClassSymbol> {

	public MethodCallConstraintGeneratorBase(
			ExprConstraintGenerator exprConstraintGenerator, A ast) {
		super(exprConstraintGenerator, ast);
	}

	@Override
	protected Set<ClassSymbol> getPossibleReceiverTypes() {
		int argumentCount = getArguments().size();
		Collection<MethodSymbol> methodSymbols = generator.getContext()
				.getMatchingMethods(getMethodName(), argumentCount);

		Set<ClassSymbol> possibleReceiverTypes = new HashSet<>();

		for (MethodSymbol methodSymbol : methodSymbols) {
			// Generate a conditional constraint for each of the subtypes of
			// the method's owner. The receiver type set may only contain a
			// subtype of the method's owner that does NOT override the
			// method. Thus, if we only created a constraint whose condition
			// only checks if the method's owner is in the receiver type
			// set, the condition would never be satisfied.
			Set<ClassSymbol> msymClassSubtypes = generator.getTypeSymbols()
					.getClassSymbolSubtypes(methodSymbol.getOwner());
			possibleReceiverTypes.addAll(msymClassSubtypes);
		}
		return possibleReceiverTypes;
	}

	@Override
	protected List<? extends TypeSet> getParameterTypeSets(
			ClassSymbol receiverType) {
		MethodSymbol methodSymbol = receiverType.getMethod(getMethodName());
		List<TypeSet> result = new ArrayList<>();
		for (VariableSymbol parameter : methodSymbol.getParameters()) {
			TypeSet parameterTypeSet = generator.getContext()
					.getVariableTypeSet(parameter);
			result.add(parameterTypeSet);
		}
		return result;
	}

	@Override
	protected TypeSet getResultTypeSet(ClassSymbol receiverType) {
		MethodSymbol methodSymbol = receiverType.getMethod(getMethodName());
		return generator.getContext().getReturnTypeSet(methodSymbol);
	}

	protected abstract String getMethodName();

}