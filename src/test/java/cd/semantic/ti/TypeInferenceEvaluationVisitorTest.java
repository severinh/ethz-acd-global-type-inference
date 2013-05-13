package cd.semantic.ti;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import cd.CompilationContext;
import cd.ir.ast.Ast;
import cd.ir.ast.ClassDecl;
import cd.ir.ast.MethodDecl;
import cd.ir.ast.Seq;
import cd.ir.ast.VarDecl;
import cd.ir.symbols.BottomTypeSymbol;
import cd.semantic.TypeSymbolTable;
import cd.semantic.UntypedSemanticAnalyzer;

/**
 * Tests {@link TypeInferenceEvaluationVisitor}.
 */
public class TypeInferenceEvaluationVisitorTest {

	private TypeSymbolTable typeSymbols;
	private VarDecl localVarDecl;
	private MethodDecl methodDecl;
	private VarDecl fieldDecl;
	private ClassDecl classDecl;

	private TypeInferenceEvaluationVisitor evaluationVisitor;

	@Before
	public void setUp() {
		// Construct a very simple AST with a single class, field, method,
		// parameter and local variable
		localVarDecl = new VarDecl("float", "f");
		methodDecl = new MethodDecl("void", "foo", Arrays.asList("int"),
				Arrays.asList("p"), new Seq(Arrays.<Ast> asList(localVarDecl)),
				new Seq());
		fieldDecl = new VarDecl("boolean", "b");
		classDecl = new ClassDecl("Main", "Object", Arrays.asList(methodDecl,
				fieldDecl));

		CompilationContext context = new CompilationContext();
		new UntypedSemanticAnalyzer(context).check(Arrays.asList(classDecl));
		typeSymbols = context.getTypeSymbols();

		evaluationVisitor = new TypeInferenceEvaluationVisitor(typeSymbols);
	}

	@Test
	public void testNoErasure() {
		evaluationVisitor.evaluate(classDecl);
	}

	@Test(expected = AssertionError.class)
	public void testErasure() {
		GlobalTypeEraser.getInstance().eraseTypesFrom(typeSymbols);

		evaluateTypeInference();
	}

	@Test
	public void testCorrectTypeInference() {
		GlobalTypeEraser.getInstance().eraseTypesFrom(typeSymbols);

		// Simulates what type inference would do, namely replacing the bottom
		// type associated with variable symbols with the proper type
		methodDecl.sym.returnType = typeSymbols.getVoidType();
		fieldDecl.sym.setType(typeSymbols.getBooleanType());
		methodDecl.sym.getParameter(0).setType(typeSymbols.getIntType());
		methodDecl.sym.getLocal(localVarDecl.name).setType(
				typeSymbols.getFloatType());

		evaluateTypeInference();
	}

	@Test(expected = AssertionError.class)
	public void testIncorrectReturnTypeInference() {
		methodDecl.sym.returnType = typeSymbols.getIntType();

		evaluateTypeInference();
	}

	@Test(expected = AssertionError.class)
	public void testIncorrectFieldTypeInference() {
		fieldDecl.sym.setType(typeSymbols.getFloatType());

		evaluateTypeInference();
	}

	@Test(expected = AssertionError.class)
	public void testIncorrectParameterTypeInference() {
		methodDecl.sym.getParameter(0).setType(typeSymbols.getObjectType());

		evaluateTypeInference();
	}

	@Test(expected = AssertionError.class)
	public void testIncorrectLocalVariableTypeInference() {
		methodDecl.sym.getLocal(localVarDecl.name).setType(
				typeSymbols.getBooleanType());

		evaluateTypeInference();
	}

	@Test
	public void testUnknownType() {
		String unknownTypeName = BottomTypeSymbol.INSTANCE.getName();
		evaluateTypeInference(new VarDecl(unknownTypeName, "unknown"));
	}

	private void evaluateTypeInference() {
		evaluateTypeInference(classDecl);
	}

	private void evaluateTypeInference(Ast ast) {
		evaluationVisitor.evaluate(classDecl);
	}

}
