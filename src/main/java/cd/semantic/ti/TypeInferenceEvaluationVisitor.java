package cd.semantic.ti;

import java.util.List;

import org.junit.Assert;

import cd.ir.AstVisitor;
import cd.ir.ast.Ast;
import cd.ir.ast.MethodDecl;
import cd.ir.ast.VarDecl;
import cd.ir.symbols.TypeSymbol;
import cd.ir.symbols.VariableSymbol;
import cd.semantic.TypeSymbolTable;

/**
 * Visits an AST and asserts that the declared type of fields, return values,
 * parameters and local variables matches the type symbol associated with it.
 * 
 * This class is meant to be used to assert that the type inference has
 * correctly reconstructed the type information after it was erased from the
 * variable symbols (not the AST).
 * 
 * The class throws an {@link AssertionError} should such a check fail.
 */
public class TypeInferenceEvaluationVisitor extends AstVisitor<Void, Void> {

	private static final TypeInferenceEvaluationVisitor INSTANCE = new TypeInferenceEvaluationVisitor();

	public static final TypeInferenceEvaluationVisitor getInstance() {
		return INSTANCE;
	}

	private TypeInferenceEvaluationVisitor() {
		// nop
	}

	/**
	 * Convenience alias for {@link #visit(Ast, Void)}.
	 * 
	 * @param ast
	 *            the AST to evaluate
	 */
	public void evaluate(Ast ast) {
		visit(ast, null);
	}

	@Override
	public Void varDecl(VarDecl varDecl, Void arg) {
		assertInferredType(varDecl.type, varDecl.sym.getType());

		return super.varDecl(varDecl, arg);
	}

	@Override
	public Void methodDecl(MethodDecl methodDecl, Void arg) {
		// Assert that the return type matches
		assertInferredType(methodDecl.returnType, methodDecl.sym.returnType);

		// Assert that the parameter types match
		List<VariableSymbol> parameters = methodDecl.sym.getParameters();
		for (int i = 0; i < parameters.size(); i++) {
			String expectedType = methodDecl.argumentTypes.get(i);
			TypeSymbol inferredType = parameters.get(i).getType();
			assertInferredType(expectedType, inferredType);
		}

		return dfltDecl(methodDecl, arg);
	}

	private void assertInferredType(String expectedType, TypeSymbol inferredType) {
		// Ignore cases where the type is missing from the source program.
		// There is nothing to compare the inferred type to.
		if (!expectedType.equals(TypeSymbolTable.BOTTOM_TYPE_NAME)) {
			Assert.assertEquals(expectedType, inferredType.name);
		}
	}
}
