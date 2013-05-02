package cd.semantic;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import cd.exceptions.SemanticFailure;
import cd.ir.ast.BinaryOp;
import cd.ir.ast.BinaryOp.BOp;
import cd.ir.ast.Expr;
import cd.ir.ast.UnaryOp;
import cd.ir.ast.UnaryOp.UOp;
import cd.ir.ast.Var;
import cd.ir.symbols.TypeSymbol;
import cd.ir.symbols.VariableSymbol;
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

	private Var makeIntVar() {
		return Var.withSym(intVariable);
	}

	private Var makeFloatVar() {
		return Var.withSym(floatVariable);
	}

	private Var makeBooleanVar() {
		return Var.withSym(booleanVariable);
	}

	private Var makeTopVar() {
		return Var.withSym(topVariable);
	}

	private Var makeBottomVar() {
		return Var.withSym(bottomVariable);
	}

	private void assertType(TypeSymbol expectedType, Expr expr) {
		TypeSymbol actualType = type(expr);
		Assert.assertEquals(expectedType, actualType);
		Assert.assertEquals(expectedType, expr.getType());
	}

	private void assertIntType(Expr expr) {
		assertType(types.getIntType(), expr);
	}

	private void assertFloatType(Expr expr) {
		assertType(types.getFloatType(), expr);
	}

	private void assertBooleanType(Expr expr) {
		assertType(types.getBooleanType(), expr);
	}

	private void assertBottomType(Expr expr) {
		assertType(types.getBottomType(), expr);
	}

	@Test
	public void testBinaryOp() {
		for (BOp bOp : new BOp[] { BOp.B_TIMES, BOp.B_DIV, BOp.B_MOD,
				BOp.B_PLUS, BOp.B_MINUS }) {
			assertIntType(new BinaryOp(makeIntVar(), bOp, makeIntVar()));

			if (!bOp.equals(BOp.B_MOD)) {
				assertFloatType(new BinaryOp(makeFloatVar(), bOp,
						makeFloatVar()));
			}
		}
	}

	@Test(expected = SemanticFailure.class)
	public void testIncorrectBinaryOpModFloat() {
		type(new BinaryOp(makeFloatVar(), BOp.B_MOD, makeFloatVar()));
	}

	@Test(expected = SemanticFailure.class)
	public void testIncorrectBinaryOpMixedTypes() {
		type(new BinaryOp(makeIntVar(), BOp.B_PLUS, makeFloatVar()));
	}

	@Test(expected = SemanticFailure.class)
	public void testIncorrectBinaryOpNumericWithBooleans() {
		type(new BinaryOp(makeBooleanVar(), BOp.B_PLUS, makeBooleanVar()));
	}

	@Test(expected = SemanticFailure.class)
	public void testIncorrectBinaryOpNumericWithTop() {
		type(new BinaryOp(makeTopVar(), BOp.B_PLUS, makeIntVar()));
	}

	@Test
	public void testUnaryOp() {
		assertBooleanType(new UnaryOp(UOp.U_BOOL_NOT, makeBooleanVar()));
		assertIntType(new UnaryOp(UOp.U_PLUS, makeIntVar()));
		assertFloatType(new UnaryOp(UOp.U_MINUS, makeFloatVar()));
	}

	@Test(expected = SemanticFailure.class)
	public void testIncorrectUnaryOpBoolNot() {
		type(new UnaryOp(UOp.U_BOOL_NOT, makeFloatVar()));
	}

	@Test(expected = SemanticFailure.class)
	public void testIncorrectUnaryOpBoolNotTop() {
		type(new UnaryOp(UOp.U_BOOL_NOT, makeTopVar()));
	}

	@Test(expected = SemanticFailure.class)
	public void testIncorrectUnaryOpPlus() {
		type(new UnaryOp(UOp.U_PLUS, makeBooleanVar()));
	}

	@Test(expected = SemanticFailure.class)
	public void testIncorrectUnaryOpMinus() {
		type(new UnaryOp(UOp.U_MINUS, makeTopVar()));
	}

	@Test
	public void testUnaryOpBottom() {
		// Given an expression of bottom type, the typing visitor cannot know
		// yet whether applying the unary operator will yield an boolean,
		// integer or float value or a semantic failure
		for (UOp uOp : UOp.values()) {
			assertBottomType(new UnaryOp(uOp, makeBottomVar()));
		}
	}

	@Test
	public void testVar() {
		assertIntType(makeIntVar());
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

}
