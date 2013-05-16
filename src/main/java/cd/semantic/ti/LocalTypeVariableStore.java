package cd.semantic.ti;

import cd.ir.symbols.MethodSymbol;
import cd.ir.symbols.VariableSymbol;
import cd.semantic.ti.constraintSolving.ConstraintSystem;
import cd.semantic.ti.constraintSolving.TypeVariable;

public final class LocalTypeVariableStore extends TypeVariableStore {

	private LocalTypeVariableStore() {
		super();
	}

	public static LocalTypeVariableStore of(MethodSymbol methodSymbol,
			ConstraintSystem system) {
		LocalTypeVariableStore result = new LocalTypeVariableStore();

		for (VariableSymbol variable : methodSymbol.getLocalsAndParameters()) {
			String desc = variable.getName();
			TypeVariable typeVariable = system.addTypeVariable(desc);
			result.variableSymbolTypeSets.put(variable, typeVariable);
		}

		return result;
	}

}
