package cd.semantic.ti;

import cd.ir.symbols.ClassSymbol;
import cd.ir.symbols.MethodSymbol;
import cd.ir.symbols.TypeSymbol;
import cd.ir.symbols.VariableSymbol;
import cd.semantic.TypeSymbolTable;

/**
 * Sets the type of each local variable to the unknown type.
 * 
 * Since the class has no state, clients are encouraged to use the shared
 * instance provided by {@link #getInstance()}.
 */
public class LocalTypeEraser implements TypeEraser {

	private static final LocalTypeEraser INSTANCE = new LocalTypeEraser();

	public static LocalTypeEraser getInstance() {
		return INSTANCE;
	}

	protected LocalTypeEraser() {
	}

	@Override
	public void eraseTypesFrom(TypeSymbolTable symbolTable) {
		TypeSymbol bottomType = symbolTable.getBottomType();
		for (ClassSymbol classSymbol : symbolTable.getClassSymbols()) {
			for (MethodSymbol methodSymbol : classSymbol.getMethods()) {
				for (VariableSymbol localVariable : methodSymbol.getLocals()) {
					localVariable.type = bottomType;
				}
			}
		}
	}

}
