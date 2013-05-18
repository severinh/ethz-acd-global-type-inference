package cd.ir.symbols;

import org.junit.Test;

import cd.exceptions.SemanticFailure;
import cd.semantic.TypeSymbolTable;

/**
 * Tests {@link ClassSymbol}.
 */
public class ClassSymbolTest {

	@Test(expected = SemanticFailure.class)
	public void testDoubleDeclaration() {
		TypeSymbolTable typeSymbols = new TypeSymbolTable();
		ClassSymbol classSymbol = new ClassSymbol("Main",
				typeSymbols.getObjectType());

		classSymbol.addField(new VariableSymbol("foo", classSymbol));
		classSymbol.addField(new VariableSymbol("foo", classSymbol));

		classSymbol.addMethod(new MethodSymbol("main", classSymbol));
		classSymbol.addMethod(new MethodSymbol("main", classSymbol));
	}

}
