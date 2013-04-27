package cd.semantic;

import cd.debug.AstOneLine;
import cd.exceptions.SemanticFailure;
import cd.exceptions.SemanticFailure.Cause;
import cd.ir.ast.Assign;
import cd.ir.ast.Ast;
import cd.ir.ast.BinaryOp;
import cd.ir.ast.BinaryOp.BOp;
import cd.ir.ast.BooleanConst;
import cd.ir.ast.BuiltInRead;
import cd.ir.ast.BuiltInReadFloat;
import cd.ir.ast.BuiltInWrite;
import cd.ir.ast.BuiltInWriteFloat;
import cd.ir.ast.BuiltInWriteln;
import cd.ir.ast.Cast;
import cd.ir.ast.Expr;
import cd.ir.ast.Field;
import cd.ir.ast.FloatConst;
import cd.ir.ast.IfElse;
import cd.ir.ast.Index;
import cd.ir.ast.IntConst;
import cd.ir.ast.MethodCall;
import cd.ir.ast.MethodCallExpr;
import cd.ir.ast.MethodDecl;
import cd.ir.ast.NewArray;
import cd.ir.ast.NewObject;
import cd.ir.ast.NullConst;
import cd.ir.ast.ReturnStmt;
import cd.ir.ast.ThisRef;
import cd.ir.ast.UnaryOp;
import cd.ir.ast.Var;
import cd.ir.ast.WhileLoop;
import cd.ir.symbols.ArrayTypeSymbol;
import cd.ir.symbols.ClassSymbol;
import cd.ir.symbols.MethodSymbol;
import cd.ir.symbols.PrimitiveTypeSymbol;
import cd.ir.symbols.TypeSymbol;
import cd.ir.symbols.VariableSymbol;
import cd.ir.AstVisitor;
import cd.ir.ExprVisitor;

public class TypeChecker {

	private final TypeSymbolTable typeSymbols;
	private final TypingVisitor visitor = new TypingVisitor();

	private MethodDecl thisMethod;

	public TypeChecker(TypeSymbolTable typeSymbols) {
		this.typeSymbols = typeSymbols;
	}

	public TypeSymbol type(Expr expr, SymbolTable<VariableSymbol> locals) {
		return visitor.visit(expr, locals);
	}

	/**
	 * Checks whether two expressions have the same type and throws an exception
	 * otherwise.
	 * 
	 * @return The common type of the two expression.
	 */
	public TypeSymbol typeEquality(Expr leftExpr, Expr rightExpr,
			SymbolTable<VariableSymbol> locals) {
		TypeSymbol leftType = type(leftExpr, locals);
		TypeSymbol rightType = type(rightExpr, locals);

		if (leftType != rightType) {
			throw new SemanticFailure(Cause.TYPE_ERROR,
					"Expected operand types to be equal but found %s, %s",
					leftType, rightType);
		}

		return leftType;

	}

	public void typeIsPrimitive(TypeSymbol type) {
		if (type != typeSymbols.getIntType()
				&& type != typeSymbols.getFloatType()) {
			throw new SemanticFailure(Cause.TYPE_ERROR,
					"Expected %s or %s for operands but found type %s",
					typeSymbols.getIntType(), typeSymbols.getFloatType(), type);
		}
	}

	public void typeIsValidForOperator(TypeSymbol type, BOp operator) {
		if (operator == BOp.B_MOD && type == typeSymbols.getFloatType()) {
			throw new SemanticFailure(Cause.TYPE_ERROR,
					"Operation %s not valid for type %s", operator, type);
		}
	}

	public ClassSymbol asClass(TypeSymbol type) {
		if (type instanceof ClassSymbol)
			return (ClassSymbol) type;
		throw new SemanticFailure(Cause.TYPE_ERROR,
				"A class type was required, but %s was found", type);
	}

	public ArrayTypeSymbol asArray(TypeSymbol type) {
		if (type instanceof ArrayTypeSymbol)
			return (ArrayTypeSymbol) type;
		throw new SemanticFailure(Cause.TYPE_ERROR,
				"An array type was required, but %s was found", type);
	}

	public TypeSymbol superType(TypeSymbol sym) {
		if (sym instanceof PrimitiveTypeSymbol)
			return null;

		if (sym instanceof ArrayTypeSymbol)
			return typeSymbols.getObjectType();

		return ((ClassSymbol) sym).superClass;
	}

	public boolean subtype(TypeSymbol sup, TypeSymbol sub) {
		if (sub == typeSymbols.getNullType() && sup.isReferenceType())
			return true;
		while (sub != null) {
			if (sub == sup)
				return true;
			sub = superType(sub);
		}
		return false;
	}

	public void checkType(Expr ast, TypeSymbol expected,
			SymbolTable<VariableSymbol> locals) {
		TypeSymbol actual = type(ast, locals);
		if (!subtype(expected, actual))
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
			if (!subtype(lhs, rhs))
				throw new SemanticFailure(Cause.TYPE_ERROR,
						"Type %s cannot be assigned to %s", rhs, lhs);
			return null;
		}

		@Override
		public Void builtInWrite(BuiltInWrite ast,
				SymbolTable<VariableSymbol> locals) {
			checkType(ast.arg(), typeSymbols.getIntType(), locals);
			return null;
		}

		@Override
		public Void builtInWriteFloat(BuiltInWriteFloat ast,
				SymbolTable<VariableSymbol> locals) {
			checkType(ast.arg(), typeSymbols.getFloatType(), locals);
			return null;
		}

		@Override
		public Void builtInWriteln(BuiltInWriteln ast,
				SymbolTable<VariableSymbol> locals) {
			return null;
		}

		@Override
		public Void ifElse(IfElse ast, SymbolTable<VariableSymbol> locals) {
			checkType(ast.condition(), typeSymbols.getBooleanType(), locals);
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
			if (mthd == null)
				throw new SemanticFailure(Cause.NO_SUCH_METHOD,
						"Class %s has no method %s()", rcvrType.name,
						ast.methodName);

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
				checkType((Expr) argAst, mthd.getParameter(i++).getType(),
						locals);

			return null;

		}

		@Override
		public Void whileLoop(WhileLoop ast, SymbolTable<VariableSymbol> locals) {
			checkType(ast.condition(), typeSymbols.getBooleanType(), locals);
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
				checkType(ast.arg(), thisMethod.sym.returnType, locals);
			}

			return null;

		}

	}

	public class TypingVisitor extends
			ExprVisitor<TypeSymbol, SymbolTable<VariableSymbol>> {

		@Override
		public TypeSymbol visit(Expr ast, SymbolTable<VariableSymbol> arg) {
			ast.type = super.visit(ast, arg);
			return ast.type;
		}

		@Override
		public TypeSymbol binaryOp(BinaryOp ast,
				SymbolTable<VariableSymbol> locals) {

			switch (ast.operator) {

			case B_TIMES:
			case B_DIV:
			case B_MOD:
			case B_PLUS:
			case B_MINUS: {
				TypeSymbol type = typeEquality(ast.left(), ast.right(), locals);
				typeIsPrimitive(type);
				typeIsValidForOperator(type, ast.operator);
				return type;
			}

			case B_AND:
			case B_OR:
				checkType(ast.left(), typeSymbols.getBooleanType(), locals);
				checkType(ast.right(), typeSymbols.getBooleanType(), locals);
				return typeSymbols.getBooleanType();

			case B_EQUAL:
			case B_NOT_EQUAL:
				TypeSymbol left = type(ast.left(), locals);
				TypeSymbol right = type(ast.right(), locals);
				if (subtype(left, right) || subtype(right, left))
					return typeSymbols.getBooleanType();
				throw new SemanticFailure(Cause.TYPE_ERROR,
						"Types %s and %s could never be equal", left, right);

			case B_LESS_THAN:
			case B_LESS_OR_EQUAL:
			case B_GREATER_THAN:
			case B_GREATER_OR_EQUAL: {
				TypeSymbol type = typeEquality(ast.left(), ast.right(), locals);
				typeIsPrimitive(type);
				return typeSymbols.getBooleanType();
			}

			}
			throw new RuntimeException("Unhandled operator " + ast.operator);
		}

		@Override
		public TypeSymbol booleanConst(BooleanConst ast,
				SymbolTable<VariableSymbol> arg) {
			return typeSymbols.getBooleanType();
		}

		@Override
		public TypeSymbol builtInRead(BuiltInRead ast,
				SymbolTable<VariableSymbol> arg) {
			return typeSymbols.getIntType();
		}

		@Override
		public TypeSymbol builtInReadFloat(BuiltInReadFloat ast,
				SymbolTable<VariableSymbol> arg) {
			return typeSymbols.getFloatType();
		}

		@Override
		public TypeSymbol cast(Cast ast, SymbolTable<VariableSymbol> locals) {
			TypeSymbol argType = type(ast.arg(), locals);
			ast.typeSym = typeSymbols.getType(ast.typeName);

			if (subtype(argType, ast.typeSym) || subtype(ast.typeSym, argType))
				return ast.typeSym;

			throw new SemanticFailure(Cause.TYPE_ERROR,
					"Types %s and %s in cast are completely unrelated.",
					argType, ast.typeSym);
		}

		@Override
		protected TypeSymbol dfltExpr(Expr ast, SymbolTable<VariableSymbol> arg) {
			throw new RuntimeException("Unhandled type");
		}

		@Override
		public TypeSymbol field(Field ast, SymbolTable<VariableSymbol> locals) {
			ClassSymbol argType = asClass(type(ast.arg(), locals)); // class of
																	// the
																	// receiver
																	// of the
																	// field
																	// access
			ast.sym = argType.getField(ast.fieldName);
			if (ast.sym == null)
				throw new SemanticFailure(Cause.NO_SUCH_FIELD,
						"Type %s has no field %s", argType, ast.fieldName);
			return ast.sym.getType();
		}

		@Override
		public TypeSymbol index(Index ast, SymbolTable<VariableSymbol> locals) {
			ArrayTypeSymbol argType = asArray(type(ast.left(), locals));
			checkType(ast.right(), typeSymbols.getIntType(), locals);
			return argType.elementType;
		}

		@Override
		public TypeSymbol intConst(IntConst ast, SymbolTable<VariableSymbol> arg) {
			return typeSymbols.getIntType();
		}

		@Override
		public TypeSymbol floatConst(FloatConst ast,
				SymbolTable<VariableSymbol> arg) {
			return typeSymbols.getFloatType();
		}

		@Override
		public TypeSymbol newArray(NewArray ast,
				SymbolTable<VariableSymbol> locals) {
			checkType(ast.arg(), typeSymbols.getIntType(), locals);
			return typeSymbols.getType(ast.typeName);
		}

		@Override
		public TypeSymbol newObject(NewObject ast,
				SymbolTable<VariableSymbol> arg) {
			return typeSymbols.getType(ast.typeName);
		}

		@Override
		public TypeSymbol nullConst(NullConst ast,
				SymbolTable<VariableSymbol> arg) {
			return typeSymbols.getNullType();
		}

		@Override
		public TypeSymbol thisRef(ThisRef ast,
				SymbolTable<VariableSymbol> locals) {
			VariableSymbol vsym = locals.get("this");
			return vsym.getType();
		}

		@Override
		public TypeSymbol unaryOp(UnaryOp ast,
				SymbolTable<VariableSymbol> locals) {

			switch (ast.operator) {
			case U_PLUS:
			case U_MINUS: {
				TypeSymbol type = type(ast.arg(), locals);
				typeIsPrimitive(type);
				return type;
			}

			case U_BOOL_NOT:
				checkType(ast.arg(), typeSymbols.getBooleanType(), locals);
				return typeSymbols.getBooleanType();
			}
			throw new RuntimeException("Unknown unary op " + ast.operator);
		}

		@Override
		public TypeSymbol var(Var ast, SymbolTable<VariableSymbol> locals) {
			if (!locals.contains(ast.name))
				throw new SemanticFailure(Cause.NO_SUCH_VARIABLE,
						"No variable %s was found", ast.name);
			ast.setSymbol(locals.get(ast.name));
			return ast.sym.getType();
		}

		@Override
		public TypeSymbol methodCall(MethodCallExpr ast,
				SymbolTable<VariableSymbol> locals) {

			ClassSymbol rcvrType = asClass(type(ast.receiver(), locals));
			MethodSymbol mthd = rcvrType.getMethod(ast.methodName);
			if (mthd == null)
				throw new SemanticFailure(Cause.NO_SUCH_METHOD,
						"Class %s has no method %s()", rcvrType.name,
						ast.methodName);

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
				checkType((Expr) argAst, mthd.getParameters().get(i++)
						.getType(), locals);

			return ast.sym.returnType;

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
