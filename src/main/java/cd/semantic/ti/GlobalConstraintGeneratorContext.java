package cd.semantic.ti;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import cd.ir.symbols.ClassSymbol;
import cd.ir.symbols.MethodSymbol;
import cd.ir.symbols.VariableSymbol;
import cd.semantic.TypeSymbolTable;
import cd.semantic.ti.constraintSolving.TypeVariable;

public final class GlobalConstraintGeneratorContext extends
		ConstraintGeneratorContext {

	private final Map<MethodSymbol, TypeVariable> returnTypeSets;

	private GlobalConstraintGeneratorContext(TypeSymbolTable typeSymbols) {
		super(typeSymbols);

		this.returnTypeSets = new HashMap<>();
	}

	public static GlobalConstraintGeneratorContext of(
			TypeSymbolTable typeSymbols) {
		GlobalConstraintGeneratorContext result = new GlobalConstraintGeneratorContext(
				typeSymbols);
		for (ClassSymbol classSymbol : typeSymbols.getClassSymbols()) {
			String prefix = classSymbol.getName() + "_";

			for (MethodSymbol methodSymbol : classSymbol.getDeclaredMethods()) {
				String methodPrefix = prefix + methodSymbol.getName() + "_";

				for (VariableSymbol variable : methodSymbol
						.getLocalsAndParameters()) {
					String desc = prefix + variable.getName();
					TypeVariable typeVariable = result.getConstraintSystem()
							.addTypeVariable(desc);
					result.variableSymbolTypeSets.put(variable, typeVariable);
				}

				TypeVariable returnTypeVariable = result.getConstraintSystem()
						.addTypeVariable(methodPrefix + "return");
				result.returnTypeSets.put(methodSymbol, returnTypeVariable);
			}

			for (VariableSymbol field : classSymbol.getDeclaredFields()) {
				String desc = prefix + field.getName();
				TypeVariable fieldTypeVariable = result.getConstraintSystem()
						.addTypeVariable(desc);
				result.variableSymbolTypeSets.put(field, fieldTypeVariable);
			}
		}
		return result;
	}

	public Map<MethodSymbol, TypeVariable> getReturnTypeSets() {
		return Collections.unmodifiableMap(returnTypeSets);
	}

	@Override
	public TypeVariable getReturnTypeSet(MethodSymbol method) {
		return returnTypeSets.get(method);
	}

}
