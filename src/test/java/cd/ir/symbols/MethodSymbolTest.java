package cd.ir.symbols;

import org.junit.Before;
import org.junit.Test;

import cd.exceptions.SemanticFailure;
import cd.semantic.TypeSymbolTable;

/**
 * Tests {@link MethodSymbol}.
 */
public class MethodSymbolTest {

	@Test(expected = SemanticFailure.class)
	public void testDoubleDeclaration() {
		TypeSymbolTable typeSymbols = new TypeSymbolTable();
		ClassSymbol classSymbol = new ClassSymbol("Main",
				typeSymbols.getObjectType());
		MethodSymbol methodSymbol = new MethodSymbol("main", classSymbol);

		methodSymbol.addLocal(new VariableSymbol("foo", classSymbol));
		methodSymbol.addLocal(new VariableSymbol("foo", classSymbol));

		methodSymbol.addParameter(new VariableSymbol("foo", classSymbol));
		methodSymbol.addParameter(new VariableSymbol("foo", classSymbol));
	}

}
