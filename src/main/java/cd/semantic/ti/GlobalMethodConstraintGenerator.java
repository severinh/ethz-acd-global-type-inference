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
	public TypeSet getVariableTypeSet(VariableSymbol localVariable) {
		return typeSetStore.getVariableSymbolTypeSet(localVariable);
	}

	@Override
	public TypeSet getReturnTypeSet(MethodSymbol method) {
		return typeSetStore.getReturnTypeSet(method);
	}

}
