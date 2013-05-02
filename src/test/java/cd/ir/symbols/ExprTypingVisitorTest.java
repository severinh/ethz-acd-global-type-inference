package cd.ir.symbols;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import cd.exceptions.SemanticFailure;
import cd.ir.ast.Expr;
import cd.ir.ast.Var;
import cd.semantic.ExprTypingVisitor;
import cd.semantic.SymbolTable;
import cd.semantic.TypeSymbolTable;

/**
 * Tests {@link ExprTypingVisitor}.
 */
public class ExprTypingVisitorTest {

	private TypeSymbolTable types;
	private ExprTypingVisitor visitor;
	private SymbolTable<VariableSymbol> variables;

	@Before
	public void setUp() {
		types = new TypeSymbolTable();
		visitor = new ExprTypingVisitor(types);
		variables = new SymbolTable<>();
	}

	@Test
	public void testVar() {
		addVariableSymbol("i", types.getIntType());
		assertType(types.getIntType(), new Var("i"));
	}

	@Test(expected = SemanticFailure.class)
	public void testUnknownVar() {
		type(new Var("unknown"));
	}

	private VariableSymbol addVariableSymbol(String name, TypeSymbol type) {
		VariableSymbol symbol = new VariableSymbol(name, type);
		variables.add(symbol);
		return symbol;
	}

	private TypeSymbol type(Expr expr) {
		return visitor.type(expr, variables);
	}

	private void assertType(TypeSymbol expectedType, Expr expr) {
		TypeSymbol actualType = type(expr);
		Assert.assertEquals(expectedType, actualType);
		Assert.assertEquals(expectedType, expr.getType());
	}

}
