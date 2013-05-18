package cd.semantic;

import cd.exceptions.SemanticFailure;
import cd.exceptions.SemanticFailure.Cause;
import cd.ir.ExprVisitor;
import cd.ir.ast.Ast;
import cd.ir.ast.BinaryOp;
import cd.ir.ast.BooleanConst;
import cd.ir.ast.BuiltInRead;
import cd.ir.ast.BuiltInReadFloat;
import cd.ir.ast.Cast;
import cd.ir.ast.Expr;
import cd.ir.ast.Field;
import cd.ir.ast.FloatConst;
import cd.ir.ast.Index;
import cd.ir.ast.IntConst;
import cd.ir.ast.MethodCallExpr;
import cd.ir.ast.NewArray;
import cd.ir.ast.NewObject;
import cd.ir.ast.NullConst;
import cd.ir.ast.ThisRef;
import cd.ir.ast.UnaryOp;
import cd.ir.ast.Var;
import cd.ir.ast.BinaryOp.BOp;
import cd.ir.symbols.ArrayTypeSymbol;
import cd.ir.symbols.ClassSymbol;
import cd.ir.symbols.MethodSymbol;
import cd.ir.symbols.TypeSymbol;
import cd.ir.symbols.VariableSymbol;

public class ExprTypingVisitor extends
		ExprVisitor<TypeSymbol, SymbolTable<VariableSymbol>> {

	private final TypeSymbolTable typeSymbols;

	public ExprTypingVisitor(TypeSymbolTable typeSymbols) {
		this.typeSymbols = typeSymbols;
	}

	@Override
	public TypeSymbol visit(Expr ast, SymbolTable<VariableSymbol> scope) {
		TypeSymbol type = super.visit(ast, scope);
		ast.setType(type);
		return type;
	}

	/**
	 * Convenience alias for {@link #visit(Expr, SymbolTable)}.
	 */
	public TypeSymbol type(Expr expr, SymbolTable<VariableSymbol> scope) {
		return visit(expr, scope);
	}

	/**
	 * @todo Copied from {@link TypeChecker}. Once the method does not depend on
	 *       the type symbol table anymore, it should be possible to get rid of
	 *       this redundancy again
	 */
	public void checkType(TypeSymbol expectedType, Expr ast,
			SymbolTable<VariableSymbol> scope) {
		TypeSymbol actualType = type(ast, scope);
		checkType(expectedType, actualType);
	}

	public void checkType(TypeSymbol expectedType, TypeSymbol actualType) {
		if (!typeSymbols.isSubType(expectedType, actualType)) {
			throw new SemanticFailure(Cause.TYPE_ERROR,
					"Expected %s but type was %s", expectedType, actualType);
		}
	}

	public void checkNumericalType(TypeSymbol type) {
		// The bottom type is also admissible, because it is a sub-type of both
		// int and float.
		boolean isNumericalSubType = false;
		for (TypeSymbol numericalType : typeSymbols.getNumericalTypeSymbols()) {
			if (typeSymbols.isSubType(numericalType, type)) {
				isNumericalSubType = true;
				break;
			}
		}
		if (!isNumericalSubType) {
			throw new SemanticFailure(Cause.TYPE_ERROR,
					"Expected numerical operand type but found type %s", type);
		}
	}

	public ArrayTypeSymbol asArray(TypeSymbol type) {
		if (type instanceof ArrayTypeSymbol) {
			return (ArrayTypeSymbol) type;
		}
		throw new SemanticFailure(Cause.TYPE_ERROR,
				"An array type was required, but %s was found", type);
	}

	@Override
	public TypeSymbol binaryOp(BinaryOp binaryOp,
			SymbolTable<VariableSymbol> scope) {
		BOp op = binaryOp.operator;

		TypeSymbol leftType = type(binaryOp.left(), scope);
		TypeSymbol rightType = type(binaryOp.right(), scope);
		TypeSymbol lcaType = typeSymbols.getLCA(leftType, rightType);
		TypeSymbol resultType;

		if (lcaType.equals(typeSymbols.getTopType())) {
			throw new SemanticFailure(Cause.TYPE_ERROR,
					"LCA of operand types %s and %s is the top type", leftType,
					rightType);
		}

		switch (op) {
		case B_TIMES:
		case B_DIV:
		case B_MOD:
		case B_PLUS:
		case B_MINUS:
			// Open for debate:
			// Using the LCA type as the resulting type of a numerical operation
			// seems quite logical. If for instance, the multiplication of a
			// float and an integer value is handled in a natural way. The
			// resulting type is the top type and this directly indicates a
			// semantic failure.
			resultType = lcaType;

			if (op == BOp.B_MOD && resultType == typeSymbols.getFloatType()) {
				throw new SemanticFailure(Cause.TYPE_ERROR,
						"Operation %s not valid for type %s", op, resultType);
			}
			checkNumericalType(resultType);
			break;
		case B_AND:
		case B_OR:
			checkType(typeSymbols.getBooleanType(), lcaType);
			resultType = typeSymbols.getBooleanType();
			break;
		case B_EQUAL:
		case B_NOT_EQUAL:
			if (!typeSymbols.isSubType(leftType, rightType)
					&& !typeSymbols.isSubType(rightType, leftType)) {
				throw new SemanticFailure(Cause.TYPE_ERROR,
						"Types %s and %s could never be equal", leftType,
						rightType);
			}
			resultType = typeSymbols.getBooleanType();
			break;
		case B_LESS_THAN:
		case B_LESS_OR_EQUAL:
		case B_GREATER_THAN:
		case B_GREATER_OR_EQUAL:
			checkNumericalType(lcaType);
			resultType = typeSymbols.getBooleanType();
			break;
		default:
			throw new RuntimeException("Unhandled operator " + op);
		}
		return resultType;
	}

	@Override
	public TypeSymbol booleanConst(BooleanConst booleanConst,
			SymbolTable<VariableSymbol> scope) {
		return typeSymbols.getBooleanType();
	}

	@Override
	public TypeSymbol builtInRead(BuiltInRead read,
			SymbolTable<VariableSymbol> scope) {
		return typeSymbols.getIntType();
	}

	@Override
	public TypeSymbol builtInReadFloat(BuiltInReadFloat readFloat,
			SymbolTable<VariableSymbol> scope) {
		return typeSymbols.getFloatType();
	}

	@Override
	public TypeSymbol cast(Cast cast, SymbolTable<VariableSymbol> scope) {
		TypeSymbol argType = type(cast.arg(), scope);
		cast.typeSym = typeSymbols.getType(cast.typeName);

		if (typeSymbols.isSubType(argType, cast.typeSym)
				|| typeSymbols.isSubType(cast.typeSym, argType)) {
			return cast.typeSym;
		}

		throw new SemanticFailure(Cause.TYPE_ERROR,
				"Types %s and %s in cast are completely unrelated.", argType,
				cast.typeSym);
	}

	@Override
	protected TypeSymbol dfltExpr(Expr expr, SymbolTable<VariableSymbol> scope) {
		throw new RuntimeException("Unhandled type");
	}

	@Override
	public TypeSymbol field(Field field, SymbolTable<VariableSymbol> scope) {
		TypeSymbol argType = type(field.arg(), scope);

		// Class of the receiver of the field access
		ClassSymbol argClass = TypeChecker.asClass(argType);
		VariableSymbol fieldSymbol = argClass.getField(field.fieldName);

		field.sym = fieldSymbol;
		return fieldSymbol.getType();
	}

	@Override
	public TypeSymbol index(Index index, SymbolTable<VariableSymbol> scope) {
		checkType(typeSymbols.getIntType(), index.right(), scope);

		TypeSymbol arrayType = type(index.left(), scope);
		// Do not complain if the type of the array variable is the bottom type.
		// Instead, return the bottom type as the element type as well
		if (arrayType == typeSymbols.getBottomType()) {
			return typeSymbols.getBottomType();
		} else {
			return asArray(arrayType).elementType;
		}
	}

	@Override
	public TypeSymbol intConst(IntConst intConst,
			SymbolTable<VariableSymbol> scope) {
		return typeSymbols.getIntType();
	}

	@Override
	public TypeSymbol floatConst(FloatConst floatConst,
			SymbolTable<VariableSymbol> scope) {
		return typeSymbols.getFloatType();
	}

	@Override
	public TypeSymbol newArray(NewArray newArray,
			SymbolTable<VariableSymbol> scope) {
		checkType(typeSymbols.getIntType(), newArray.arg(), scope);
		return typeSymbols.getType(newArray.typeName);
	}

	@Override
	public TypeSymbol newObject(NewObject newObject,
			SymbolTable<VariableSymbol> scope) {
		return typeSymbols.getType(newObject.typeName);
	}

	@Override
	public TypeSymbol nullConst(NullConst nullConst,
			SymbolTable<VariableSymbol> scope) {
		return typeSymbols.getNullType();
	}

	@Override
	public TypeSymbol thisRef(ThisRef thisRef, SymbolTable<VariableSymbol> scope) {
		VariableSymbol vsym = scope.get("this");
		return vsym.getType();
	}

	@Override
	public TypeSymbol unaryOp(UnaryOp unaryOp, SymbolTable<VariableSymbol> scope) {
		TypeSymbol type = type(unaryOp.arg(), scope);
		switch (unaryOp.operator) {
		case U_PLUS:
		case U_MINUS:
			checkNumericalType(type);
			return type;
		case U_BOOL_NOT:
			checkType(typeSymbols.getBooleanType(), type);
			return typeSymbols.getBooleanType();
		}
		throw new RuntimeException("Unknown unary op " + unaryOp.operator);
	}

	@Override
	public TypeSymbol var(Var var, SymbolTable<VariableSymbol> scope) {
		VariableSymbol symbol = scope.get(var.getName());
		if (symbol == null) {
			throw new SemanticFailure(Cause.NO_SUCH_VARIABLE,
					"No variable %s was found", var.getName());
		}
		var.setSymbol(symbol);
		return symbol.getType();
	}

	@Override
	public TypeSymbol methodCall(MethodCallExpr methodCall,
			SymbolTable<VariableSymbol> scope) {
		ClassSymbol rcvrType = TypeChecker.asClass(type(methodCall.receiver(),
				scope));
		MethodSymbol mthd = rcvrType.getMethod(methodCall.methodName);
		if (mthd == null) {
			throw new SemanticFailure(Cause.NO_SUCH_METHOD,
					"Class %s has no method %s()", rcvrType.name,
					methodCall.methodName);
		}

		methodCall.sym = mthd;

		// Check that the number of arguments is correct.
		if (methodCall.argumentsWithoutReceiver().size() != mthd
				.getParameters().size()) {
			throw new SemanticFailure(Cause.WRONG_NUMBER_OF_ARGUMENTS,
					"Method %s() takes %d arguments, but was invoked with %d",
					methodCall.methodName, mthd.getParameters().size(),
					methodCall.argumentsWithoutReceiver().size());
		}

		// Check that the arguments are of correct type.
		int i = 0;
		for (Ast argAst : methodCall.argumentsWithoutReceiver()) {
			checkType(mthd.getParameters().get(i++).getType(), (Expr) argAst,
					scope);
		}

		return methodCall.sym.returnType;

	}

}