package cd.semantic;

import cd.debug.AstOneLine;
import cd.exceptions.SemanticFailure;
import cd.exceptions.SemanticFailure.Cause;
import cd.ir.AstVisitor;
import cd.ir.ExprVisitor;
import cd.ir.ast.Assign;
import cd.ir.ast.Ast;
import cd.ir.ast.BuiltInTick;
import cd.ir.ast.BuiltInTock;
import cd.ir.ast.BuiltInWrite;
import cd.ir.ast.BuiltInWriteFloat;
import cd.ir.ast.BuiltInWriteln;
import cd.ir.ast.Expr;
import cd.ir.ast.Field;
import cd.ir.ast.IfElse;
import cd.ir.ast.Index;
import cd.ir.ast.MethodCall;
import cd.ir.ast.MethodDecl;
import cd.ir.ast.ReturnStmt;
import cd.ir.ast.Var;
import cd.ir.ast.WhileLoop;
import cd.ir.symbols.ClassSymbol;
import cd.ir.symbols.MethodSymbol;
import cd.ir.symbols.TypeSymbol;
import cd.ir.symbols.VariableSymbol;

public class TypeChecker {

	private final TypeSymbolTable typeSymbols;
	private final ExprTypingVisitor visitor;

	private MethodDecl thisMethod;

	public TypeChecker(TypeSymbolTable typeSymbols) {
		this.typeSymbols = typeSymbols;
		this.visitor = new ExprTypingVisitor(typeSymbols);
	}

	public TypeSymbol type(Expr expr, SymbolTable<VariableSymbol> locals) {
		return visitor.visit(expr, locals);
	}

	public static ClassSymbol asClass(TypeSymbol type) {
		if (type instanceof ClassSymbol)
			return (ClassSymbol) type;
		throw new SemanticFailure(Cause.TYPE_ERROR,
				"A class type was required, but %s was found", type);
	}

	public void checkType(TypeSymbol expected, Expr ast,
			SymbolTable<VariableSymbol> locals) {
		TypeSymbol actual = type(ast, locals);
		if (!typeSymbols.isSubType(expected, actual))
			throw new SemanticFailure(Cause.TYPE_ERROR,
					"Expected %s but type was %s", expected, actual);
	}

	public void checkStmt(MethodDecl method, SymbolTable<VariableSymbol> locals) {
		this.thisMethod = method;
		new StmtVisitor().visit(method.body(), locals);
	}

	public class StmtVisitor extends
			AstVisitor<Void, SymbolTable<VariableSymbol>> {

		@Override
		protected Void dfltExpr(Expr ast, SymbolTable<VariableSymbol> locals) {
			throw new RuntimeException("Should not get here");
		}

		@Override
		public Void assign(Assign ast, SymbolTable<VariableSymbol> locals) {
			TypeSymbol lhs = typeLhs(ast.left(), locals);
			TypeSymbol rhs = type(ast.right(), locals);
			if (!typeSymbols.isSubType(lhs, rhs))
				throw new SemanticFailure(Cause.TYPE_ERROR,
						"Type %s cannot be assigned to %s", rhs, lhs);
			return null;
		}

		@Override
		public Void builtInWrite(BuiltInWrite ast,
				SymbolTable<VariableSymbol> locals) {
			checkType(typeSymbols.getIntType(), ast.arg(), locals);
			return null;
		}

		@Override
		public Void builtInWriteFloat(BuiltInWriteFloat ast,
				SymbolTable<VariableSymbol> locals) {
			checkType(typeSymbols.getFloatType(), ast.arg(), locals);
			return null;
		}

		@Override
		public Void builtInWriteln(BuiltInWriteln ast,
				SymbolTable<VariableSymbol> locals) {
			return null;
		}
		
		@Override
		public Void builtInTick(BuiltInTick ast,
				SymbolTable<VariableSymbol> locals) {
			return null;
		}
		
		@Override
		public Void builtInTock(BuiltInTock ast,
				SymbolTable<VariableSymbol> locals) {
			return null;
		}

		@Override
		public Void ifElse(IfElse ast, SymbolTable<VariableSymbol> locals) {
			checkType(typeSymbols.getBooleanType(), ast.condition(), locals);
			visit(ast.then(), locals);
			if (ast.otherwise() != null)
				visit(ast.otherwise(), locals);
			return null;
		}

		@Override
		public Void methodCall(MethodCall ast,
				SymbolTable<VariableSymbol> locals) {

			ClassSymbol rcvrType = asClass(type(ast.receiver(), locals));
			MethodSymbol mthd = rcvrType.getMethod(ast.methodName);

			ast.sym = mthd;

			// Check that the number of arguments is correct.
			if (ast.argumentsWithoutReceiver().size() != mthd.getParameters()
					.size())
				throw new SemanticFailure(
						Cause.WRONG_NUMBER_OF_ARGUMENTS,
						"Method %s() takes %d arguments, but was invoked with %d",
						ast.methodName, mthd.getParameters().size(), ast
								.argumentsWithoutReceiver().size());

			// Check that the arguments are of correct type.
			int i = 0;
			for (Ast argAst : ast.argumentsWithoutReceiver())
				checkType(mthd.getParameter(i++).getType(), (Expr) argAst,
						locals);

			return null;

		}

		@Override
		public Void whileLoop(WhileLoop ast, SymbolTable<VariableSymbol> locals) {
			checkType(typeSymbols.getBooleanType(), ast.condition(), locals);
			return visit(ast.body(), locals);
		}

		@Override
		public Void returnStmt(ReturnStmt ast,
				SymbolTable<VariableSymbol> locals) {

			if (ast.arg() == null) {

				if (thisMethod.sym.returnType != typeSymbols.getVoidType()) {

					throw new SemanticFailure(
							Cause.TYPE_ERROR,
							"Return statement has no arguments. Expected %s but type was %s",
							thisMethod.sym.returnType, typeSymbols
									.getVoidType());
				}

			} else {
				checkType(thisMethod.sym.returnType, ast.arg(), locals);
			}

			return null;

		}

	}

	/**
	 * Evaluates an expr as the left-hand-side of an assignment, returning the
	 * type of value that may be assigned there. May fail if the expression is
	 * not a valid LHS (for example, a "final" field).
	 */
	public TypeSymbol typeLhs(Expr expr, SymbolTable<VariableSymbol> locals) {
		return new LValueVisitor().visit(expr, locals);
	}

	/**
	 * @see TypeChecker#typeLhs(Expr, SymbolTable)
	 */
	class LValueVisitor extends
			ExprVisitor<TypeSymbol, SymbolTable<VariableSymbol>> {

		/** Any other kind of expression is not a value lvalue */
		@Override
		protected TypeSymbol dfltExpr(Expr ast,
				SymbolTable<VariableSymbol> locals) {
			throw new SemanticFailure(Cause.NOT_ASSIGNABLE,
					"'%s' is not a valid lvalue", AstOneLine.toString(ast));
		}

		/** A field can always be assigned to. */
		@Override
		public TypeSymbol field(Field ast, SymbolTable<VariableSymbol> locals) {
			TypeSymbol ts = type(ast, locals);
			return ts;
		}

		/**
		 * An array dereference can always be assigned to
		 */
		@Override
		public TypeSymbol index(Index ast, SymbolTable<VariableSymbol> locals) {
			return type(ast, locals);
		}

		/** A variable can always be assigned to. */
		@Override
		public TypeSymbol var(Var ast, SymbolTable<VariableSymbol> locals) {
			TypeSymbol ts = type(ast, locals);
			return ts;
		}

	}

}
