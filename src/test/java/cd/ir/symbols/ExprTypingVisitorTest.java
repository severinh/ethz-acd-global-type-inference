package cd.ir.symbols;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import cd.exceptions.SemanticFailure;
import cd.ir.ast.Expr;
import cd.ir.ast.UnaryOp;
import cd.ir.ast.UnaryOp.UOp;
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
	private SymbolTable<VariableSymbol> scope;

	private VariableSymbol intVariable;
	private VariableSymbol floatVariable;
	private VariableSymbol booleanVariable;
	private VariableSymbol topVariable;
	private VariableSymbol bottomVariable;

	@Before
	public void setUp() {
		types = new TypeSymbolTable();
		visitor = new ExprTypingVisitor(types);
		scope = new SymbolTable<>();

		intVariable = addVariableSymbol("i", types.getIntType());
		floatVariable = addVariableSymbol("f", types.getFloatType());
		booleanVariable = addVariableSymbol("b", types.getBooleanType());
		topVariable = addVariableSymbol("top", types.getTopType());
		bottomVariable = addVariableSymbol("bottom", types.getBottomType());
	}

	@Test
	public void testUnaryOp() {
		UnaryOp unaryOp;

		unaryOp = new UnaryOp(UOp.U_BOOL_NOT, Var.withSym(booleanVariable));
		assertType(types.getBooleanType(), unaryOp);

		unaryOp = new UnaryOp(UOp.U_PLUS, Var.withSym(intVariable));
		assertType(types.getIntType(), unaryOp);

		unaryOp = new UnaryOp(UOp.U_MINUS, Var.withSym(floatVariable));
		assertType(types.getFloatType(), unaryOp);
	}

	@Test(expected = SemanticFailure.class)
	public void testIncorrectUnaryOpBoolNot() {
		type(new UnaryOp(UOp.U_BOOL_NOT, Var.withSym(floatVariable)));
	}

	@Test(expected = SemanticFailure.class)
	public void testIncorrectUnaryOpBoolNotTop() {
		type(new UnaryOp(UOp.U_BOOL_NOT, Var.withSym(topVariable)));
	}

	@Test(expected = SemanticFailure.class)
	public void testIncorrectUnaryOpPlus() {
		type(new UnaryOp(UOp.U_PLUS, Var.withSym(booleanVariable)));
	}

	@Test(expected = SemanticFailure.class)
	public void testIncorrectUnaryOpMinus() {
		type(new UnaryOp(UOp.U_MINUS, Var.withSym(topVariable)));
	}

	@Test
	public void testUnaryOpBottom() {
		// Given an expression of bottom type, the typing visitor cannot know
		// yet whether applying the unary operator will yield an boolean,
		// integer or float value or a semantic failure
		for (UOp uOp : UOp.values()) {
			UnaryOp unaryOp = new UnaryOp(uOp, Var.withSym(bottomVariable));
			assertType(types.getBottomType(), unaryOp);
		}
	}

	@Test
	public void testVar() {
		assertType(types.getIntType(), Var.withSym(intVariable));
	}

	@Test(expected = SemanticFailure.class)
	public void testUnknownVar() {
		type(new Var("unknown"));
	}

	private VariableSymbol addVariableSymbol(String name, TypeSymbol type) {
		VariableSymbol symbol = new VariableSymbol(name, type);
		scope.add(symbol);
		return symbol;
	}

	private TypeSymbol type(Expr expr) {
		return visitor.type(expr, scope);
	}

	private void assertType(TypeSymbol expectedType, Expr expr) {
		TypeSymbol actualType = type(expr);
		Assert.assertEquals(expectedType, actualType);
		Assert.assertEquals(expectedType, expr.getType());
	}

}
