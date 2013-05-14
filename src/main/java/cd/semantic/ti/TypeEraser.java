package cd.semantic.ti;

import cd.semantic.TypeSymbolTable;
import cd.util.NonnullByDefault;

/**
 * Sets the type of certain variable symbols to the unknown type.
 * 
 * This mimics the behavior of the programmer omitting type information. Type
 * erasure must not be performed after type checking has taken place.
 */
@NonnullByDefault
public interface TypeEraser {

	/**
	 * Sets the type of certain variable symbols included in the given type
	 * symbol table to the unknown type.
	 * 
	 * @param symbolTable
	 *            the symbol table
	 */
	public void eraseTypesFrom(TypeSymbolTable symbolTable);

}
