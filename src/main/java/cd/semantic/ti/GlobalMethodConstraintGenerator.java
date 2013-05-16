package cd.semantic.ti;

import cd.ir.ast.MethodDecl;
import cd.ir.symbols.MethodSymbol;
import cd.ir.symbols.VariableSymbol;
import cd.semantic.ti.constraintSolving.TypeSet;

public class GlobalMethodConstraintGenerator extends MethodConstraintGenerator {

	private final GlobalTypeVariableStore typeVariableStore;

	public GlobalMethodConstraintGenerator(MethodDecl methodDecl,
			MethodConstraintGeneratorContext context,
			GlobalTypeVariableStore variableTypeStore) {
		super(methodDecl, context);

		this.typeVariableStore = variableTypeStore;
	}

	@Override
	public TypeSet getVariableTypeSet(VariableSymbol localVariable) {
		return typeVariableStore.getVariableSymbolTypeSet(localVariable);
	}

	@Override
	public TypeSet getReturnTypeSet(MethodSymbol method) {
		return typeVariableStore.getReturnTypeSet(method);
	}

}
