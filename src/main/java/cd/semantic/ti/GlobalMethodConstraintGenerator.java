package cd.semantic.ti;

import cd.ir.ast.MethodDecl;
import cd.ir.symbols.MethodSymbol;
import cd.ir.symbols.VariableSymbol;
import cd.semantic.ti.constraintSolving.TypeSet;

public class GlobalMethodConstraintGenerator extends MethodConstraintGenerator {

	private final GlobalTypeVariableStore typeSetStore;

	public GlobalMethodConstraintGenerator(MethodDecl methodDecl,
			MethodConstraintGeneratorContext context,
			GlobalTypeVariableStore globalVariableTypeSets) {
		super(methodDecl, context);

		this.typeSetStore = globalVariableTypeSets;
	}

	@Override
	public TypeSet getLocalVariableTypeSet(VariableSymbol localVariable) {
		return typeSetStore.getVariableSymbolTypeSet(localVariable);
	}

	@Override
	public TypeSet getParameterTypeSet(VariableSymbol parameter) {
		return typeSetStore.getVariableSymbolTypeSet(parameter);
	}

	@Override
	public TypeSet getFieldTypeSet(VariableSymbol field) {
		return typeSetStore.getVariableSymbolTypeSet(field);
	}

	@Override
	public TypeSet getReturnTypeSet(MethodSymbol method) {
		return typeSetStore.getReturnTypeSet(method);
	}

}
