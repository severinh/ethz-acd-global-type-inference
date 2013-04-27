package cd.ir.symbols;

import org.junit.Before;
import org.junit.Test;

import cd.exceptions.SemanticFailure;

/**
 * Tests {@link ClassSymbol}.
 */
public class ClassSymbolTest {

	private ClassSymbol classSymbol;

	@Before
	public void setUp() {
		classSymbol = new ClassSymbol("Main");
	}

	@Test(expected = SemanticFailure.class)
	public void testMethodDoubleDeclaration() {
		classSymbol.addMethod(new MethodSymbol("main", classSymbol));
		classSymbol.addMethod(new MethodSymbol("main", classSymbol));
	}

	@Test(expected = NullPointerException.class)
	public void testNullMethod() {
		classSymbol.addMethod(null);
	}

	@Test(expected = SemanticFailure.class)
	public void testFieldDoubleDeclaration() {
		classSymbol.addField(new VariableSymbol("foo", classSymbol));
		classSymbol.addField(new VariableSymbol("foo", classSymbol));
	}

	@Test(expected = NullPointerException.class)
	public void testNullField() {
		classSymbol.addField(null);
	}

}
