package cd.ir.symbols;

import org.junit.Test;

import cd.exceptions.SemanticFailure;

/**
 * Tests {@link MethodSymbol}.
 */
public class MethodSymbolTest {

	@Test(expected = SemanticFailure.class)
	public void testLocalDoubleDeclaration() {
		ClassSymbol classSymbol = new ClassSymbol("Main");
		MethodSymbol methodSymbol = new MethodSymbol("main", classSymbol);
		methodSymbol.addLocal(new VariableSymbol("foo", classSymbol));
		methodSymbol.addLocal(new VariableSymbol("foo", classSymbol));
	}

}
