package cd.ir.symbols;

import org.junit.Before;
import org.junit.Test;

import cd.exceptions.SemanticFailure;

/**
 * Tests {@link MethodSymbol}.
 */
public class MethodSymbolTest {

	private ClassSymbol classSymbol;
	private MethodSymbol methodSymbol;

	@Before
	public void setUp() {
		classSymbol = new ClassSymbol("Main");
		methodSymbol = new MethodSymbol("main", classSymbol);
	}

	@Test(expected = SemanticFailure.class)
	public void testLocalDoubleDeclaration() {
		methodSymbol.addLocal(new VariableSymbol("foo", classSymbol));
		methodSymbol.addLocal(new VariableSymbol("foo", classSymbol));
	}

	@Test(expected = NullPointerException.class)
	public void testNullLocal() {
		methodSymbol.addLocal(null);
	}

	@Test(expected = SemanticFailure.class)
	public void testParameterDoubleDeclaration() {
		methodSymbol.addParameter(new VariableSymbol("foo", classSymbol));
		methodSymbol.addParameter(new VariableSymbol("foo", classSymbol));
	}

	@Test(expected = NullPointerException.class)
	public void testNullParameter() {
		methodSymbol.addParameter(null);
	}

}
