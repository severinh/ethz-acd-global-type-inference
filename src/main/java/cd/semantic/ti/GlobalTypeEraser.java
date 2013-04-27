package cd.semantic.ti;

import cd.ir.symbols.ClassSymbol;
import cd.ir.symbols.MethodSymbol;
import cd.ir.symbols.TypeSymbol;
import cd.ir.symbols.VariableSymbol;
import cd.semantic.TypeSymbolTable;

/**
 * Sets the type of each field, parameter, return value and local variable to
 * the unknown type.
 * 
 * Since the class has no state, clients are encouraged to use the shared
 * instance provided by {@link #getInstance()}.
 */
public class GlobalTypeEraser extends LocalTypeEraser {

	private static final GlobalTypeEraser INSTANCE = new GlobalTypeEraser();

	public static GlobalTypeEraser getInstance() {
		return INSTANCE;
	}

	protected GlobalTypeEraser() {
	}

	/**
	 * Sets the type of each field, parameter, return value and local variable
	 * in the given type symbol table to the unknown type.
	 * 
	 * @param symbolTable
	 *            the symbol table
	 */
	@Override
	public void eraseTypesFrom(TypeSymbolTable symbolTable) {
		super.eraseTypesFrom(symbolTable);

		TypeSymbol bottomType = symbolTable.getBottomType();
		for (ClassSymbol classSymbol : symbolTable.getClassSymbols()) {
			for (VariableSymbol field : classSymbol.getDeclaredFields()) {
				field.setType(bottomType);
			}
			for (MethodSymbol method : classSymbol.getDeclaredMethods()) {
				method.returnType = bottomType;
				for (VariableSymbol parameter : method.getParameters()) {
					parameter.setType(bottomType);
				}
			}
		}
	}

}
