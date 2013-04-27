package cd.ir.symbols;

import org.junit.Test;

import cd.exceptions.SemanticFailure;

/**
 * Tests {@link ClassSymbol}.
 */
public class ClassSymbolTest {

	@Test(expected = SemanticFailure.class)
	public void testMethodDoubleDeclaration() {
		ClassSymbol classSymbol = new ClassSymbol("Main");
		classSymbol.addMethod(new MethodSymbol("main", classSymbol));
		classSymbol.addMethod(new MethodSymbol("main", classSymbol));
	}

	@Test(expected = SemanticFailure.class)
	public void testFieldDoubleDeclaration() {
		ClassSymbol classSymbol = new ClassSymbol("Main");
		classSymbol.addField(new VariableSymbol("foo", classSymbol));
		classSymbol.addField(new VariableSymbol("foo", classSymbol));
	}

}
