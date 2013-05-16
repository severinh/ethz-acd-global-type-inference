package cd.semantic.ti;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import cd.ir.symbols.VariableSymbol;
import cd.semantic.ti.constraintSolving.TypeVariable;

public abstract class TypeVariableStore {

	protected final Map<VariableSymbol, TypeVariable> variableSymbolTypeSets;

	public TypeVariableStore() {
		super();

		this.variableSymbolTypeSets = new HashMap<>();
	}

	public Map<VariableSymbol, TypeVariable> getVariableSymbolTypeSets() {
		return Collections.unmodifiableMap(variableSymbolTypeSets);
	}

	public TypeVariable getVariableSymbolTypeSet(VariableSymbol variable) {
		return variableSymbolTypeSets.get(variable);
	}

}