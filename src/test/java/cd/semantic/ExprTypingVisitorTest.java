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
	private VariableSymbol objectVariable;

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
		objectVariable = addVariableSymbol("o", types.getObjectType());
	}

	private Var makeIntVar() {
		return Var.withSym(intVariable);
	}

	private Var makeFloatVar() {
		return Var.withSym(floatVariable);
	}

	private Var makeBoolVar() {
		return Var.withSym(booleanVariable);
	}

	private Var makeTopVar() {
		return Var.withSym(topVariable);
	}

	private Var makeBottomVar() {
		return Var.withSym(bottomVariable);
	}

	private Var makeObjectVar() {
		return Var.withSym(objectVariable);
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
		for (BOp op : new BOp[] { BOp.B_TIMES, BOp.B_DIV, BOp.B_MOD,
				BOp.B_PLUS, BOp.B_MINUS }) {
			assertIntType(new BinaryOp(makeIntVar(), op, makeIntVar()));

			if (!op.equals(BOp.B_MOD)) {
				assertFloatType(new BinaryOp(makeFloatVar(), op, makeFloatVar()));
			}

			assertIntType(new BinaryOp(makeIntVar(), op, makeBottomVar()));
			assertBottomType(new BinaryOp(makeBottomVar(), op, makeBottomVar()));
		}

		for (BOp op : new BOp[] { BOp.B_AND, BOp.B_OR }) {
			assertBooleanType(new BinaryOp(makeBoolVar(), op, makeBoolVar()));
			assertBooleanType(new BinaryOp(makeBoolVar(), op, makeBottomVar()));
			assertBooleanType(new BinaryOp(makeBottomVar(), op, makeBottomVar()));
		}

		for (BOp op : new BOp[] { BOp.B_EQUAL, BOp.B_NOT_EQUAL }) {
			assertBooleanType(new BinaryOp(makeIntVar(), op, makeIntVar()));
			assertBooleanType(new BinaryOp(makeBoolVar(), op, makeBoolVar()));
			assertBooleanType(new BinaryOp(makeBottomVar(), op, makeBottomVar()));
			assertBooleanType(new BinaryOp(makeBottomVar(), op, makeIntVar()));
			assertBooleanType(new BinaryOp(makeObjectVar(), op, makeObjectVar()));
		}
	}

	@Test(expected = SemanticFailure.class)
	public void testIncorrectBinaryOpModFloat() {
		type(new BinaryOp(makeFloatVar(), BOp.B_MOD, makeFloatVar()));
	}

	@Test(expected = SemanticFailure.class)
	public void testIncorrectBinaryOpNumericMixedTypes() {
		type(new BinaryOp(makeIntVar(), BOp.B_PLUS, makeFloatVar()));
	}

	@Test(expected = SemanticFailure.class)
	public void testIncorrectBinaryOpNumericWithBooleans() {
		type(new BinaryOp(makeBoolVar(), BOp.B_PLUS, makeBoolVar()));
	}

	@Test(expected = SemanticFailure.class)
	public void testIncorrectBinaryOpNumericWithTop() {
		type(new BinaryOp(makeTopVar(), BOp.B_PLUS, makeIntVar()));
	}

	@Test(expected = SemanticFailure.class)
	public void testIncorrectBinaryOpLogical() {
		type(new BinaryOp(makeIntVar(), BOp.B_AND, makeBoolVar()));
	}

	@Test(expected = SemanticFailure.class)
	public void testIncorrectBinaryOpEqualityUnrelatedTypes() {
		type(new BinaryOp(makeIntVar(), BOp.B_EQUAL, makeFloatVar()));
	}

	@Test
	public void testUnaryOp() {
		assertBooleanType(new UnaryOp(UOp.U_BOOL_NOT, makeBoolVar()));
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
		type(new UnaryOp(UOp.U_PLUS, makeBoolVar()));
	}

	@Test(expected = SemanticFailure.class)
	public void testIncorrectUnaryOpMinus() {
		type(new UnaryOp(UOp.U_MINUS, makeTopVar()));
	}

	@Test
	public void testUnaryOpBottom() {
		assertBooleanType(new UnaryOp(UOp.U_BOOL_NOT, makeBottomVar()));
		assertBottomType(new UnaryOp(UOp.U_MINUS, makeBottomVar()));
		assertBottomType(new UnaryOp(UOp.U_PLUS, makeBottomVar()));
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
