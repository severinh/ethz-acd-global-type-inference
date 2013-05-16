package cd.semantic.ti;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import cd.ir.symbols.ClassSymbol;
import cd.ir.symbols.MethodSymbol;
import cd.ir.symbols.VariableSymbol;
import cd.semantic.TypeSymbolTable;
import cd.semantic.ti.constraintSolving.ConstraintSystem;
import cd.semantic.ti.constraintSolving.TypeVariable;

public final class GlobalTypeVariableStore extends TypeVariableStore {

	private final Map<MethodSymbol, TypeVariable> returnTypeSets;

	private GlobalTypeVariableStore() {
		super();

		this.returnTypeSets = new HashMap<>();
	}

	public static GlobalTypeVariableStore of(TypeSymbolTable typeSymbols,
			ConstraintSystem system) {
		GlobalTypeVariableStore result = new GlobalTypeVariableStore();
		for (ClassSymbol classSymbol : typeSymbols.getClassSymbols()) {
			String prefix = classSymbol.getName() + "_";

			for (MethodSymbol methodSymbol : classSymbol.getDeclaredMethods()) {
				String methodPrefix = prefix + methodSymbol.getName() + "_";

				for (VariableSymbol variable : methodSymbol
						.getLocalsAndParameters()) {
					String desc = prefix + variable.getName();
					TypeVariable typeVariable = system.addTypeVariable(desc);
					result.variableSymbolTypeSets.put(variable, typeVariable);
				}

				TypeVariable returnTypeVariable = system
						.addTypeVariable(methodPrefix + "return");
				result.returnTypeSets.put(methodSymbol, returnTypeVariable);
			}

			for (VariableSymbol field : classSymbol.getDeclaredFields()) {
				String desc = prefix + field.getName();
				TypeVariable fieldTypeVariable = system.addTypeVariable(desc);
				result.variableSymbolTypeSets.put(field, fieldTypeVariable);
			}
		}
		return result;
	}

	public Map<MethodSymbol, TypeVariable> getReturnTypeSets() {
		return Collections.unmodifiableMap(returnTypeSets);
	}

	public TypeVariable getReturnTypeSet(MethodSymbol method) {
		return returnTypeSets.get(method);
	}

}
