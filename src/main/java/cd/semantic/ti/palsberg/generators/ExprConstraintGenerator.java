package cd.semantic.ti.palsberg.generators;

import cd.ir.ExprVisitorWithoutArg;
import cd.ir.ast.BinaryOp;
import cd.ir.ast.BooleanConst;
import cd.ir.ast.BuiltInRead;
import cd.ir.ast.BuiltInReadFloat;
import cd.ir.ast.Cast;
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
import cd.ir.ast.UnaryOp.UOp;
import cd.ir.symbols.MethodSymbol;
import cd.ir.symbols.PrimitiveTypeSymbol;
import cd.ir.symbols.TypeSymbol;
import cd.semantic.TypeSymbolTable;
import cd.semantic.ti.palsberg.constraints.ConstraintCondition;
import cd.semantic.ti.palsberg.solving.ConstantTypeSet;
import cd.semantic.ti.palsberg.solving.ConstantTypeSetFactory;
import cd.semantic.ti.palsberg.solving.ConstraintSystem;
import cd.semantic.ti.palsberg.solving.TypeSet;

/**
 * Recursively generates the type set for a expressions and potentially adds
 * type constraints to the constraint system.
 * 
 * The type sets associated with fields, parameters, local variables and return
 * values are looked up in the context.
 */
public class ExprConstraintGenerator extends ExprVisitorWithoutArg<TypeSet> {

	private final MethodSymbol method;
	private final ConstraintGeneratorContext context;

	public ExprConstraintGenerator(MethodSymbol method,
			ConstraintGeneratorContext context) {
		this.method = method;
		this.context = context;
	}

	ConstraintGeneratorContext getContext() {
		return context;
	}

	/**
	 * Convenience shortcut for {@code context.getConstantTypeSetFactory()}.
	 */
	ConstantTypeSetFactory getTypeSetFactory() {
		return context.getConstantTypeSetFactory();
	}

	/**
	 * Convenience shortcut for {@code context.getConstraintSystem()}.
	 */
	ConstraintSystem getSystem() {
		return context.getConstraintSystem();
	}

	/**
	 * Convenience shortcut for {@link context.getTypeSymbolTable()}.
	 */
	TypeSymbolTable getTypeSymbols() {
		return context.getTypeSymbolTable();
	}

	@Override
	public TypeSet var(Var ast) {
		return context.getVariableTypeSet(ast.getSymbol());
	}

	@Override
	public TypeSet intConst(IntConst ast) {
		return getTypeSetFactory().makeInt();
	}

	@Override
	public TypeSet floatConst(FloatConst ast) {
		return getTypeSetFactory().makeFloat();
	}

	@Override
	public TypeSet booleanConst(BooleanConst ast) {
		return getTypeSetFactory().makeBoolean();
	}

	@Override
	public TypeSet nullConst(NullConst ast) {
		return getTypeSetFactory().makeReferenceTypeSet();
	}

	@Override
	public TypeSet newObject(NewObject ast) {
		return getTypeSetFactory().make(ast.typeName);
	}

	@Override
	public TypeSet newArray(NewArray ast) {
		TypeSet sizeTypeSet = visit(ast.arg());
		getSystem().addEquality(sizeTypeSet, getTypeSetFactory().makeInt());

		return getTypeSetFactory().make(ast.typeName);
	}

	@Override
	public TypeSet thisRef(ThisRef ast) {
		return getTypeSetFactory().make(method.getOwner());
	}

	@Override
	public TypeSet cast(Cast ast) {
		TypeSet exprTypeSet = visit(ast.arg());
		// only reference types can be cast
		ConstantTypeSet allRefTyes = getTypeSetFactory().makeReferenceTypeSet();
		getSystem().addUpperBound(exprTypeSet, allRefTyes);

		TypeSymbol castResultType = getTypeSymbols().getType(ast.typeName);
		return getTypeSetFactory().makeDeclarableSubtypes(castResultType);
	}

	@Override
	public TypeSet field(Field ast) {
		return new FieldAccessConstraintGenerator(this, ast).generate();
	}

	@Override
	public TypeSet index(Index index) {
		return new ArrayAccessConstraintGenerator(this, index).generate();
	}

	@Override
	public TypeSet builtInRead(BuiltInRead ast) {
		return getTypeSetFactory().makeInt();
	}

	@Override
	public TypeSet builtInReadFloat(BuiltInReadFloat ast) {
		return getTypeSetFactory().makeFloat();
	}

	@Override
	public TypeSet binaryOp(BinaryOp binaryOp) {
		BOp op = binaryOp.operator;
		TypeSet leftTypeSet = visit(binaryOp.left());
		TypeSet rightTypeSet = visit(binaryOp.right());

		ConstantTypeSet booleanTypeSet, numTypeSet;
		numTypeSet = getTypeSetFactory().makeNumericalTypeSet();
		booleanTypeSet = getTypeSetFactory().makeBoolean();

		switch (op) {
		case B_TIMES:
		case B_DIV:
		case B_MOD:
		case B_PLUS:
		case B_MINUS:
			getSystem().addUpperBound(leftTypeSet, numTypeSet);
			getSystem().addEquality(leftTypeSet, rightTypeSet);
			return leftTypeSet;
		case B_AND:
		case B_OR:
			getSystem().addEquality(leftTypeSet, rightTypeSet);
			getSystem().addEquality(leftTypeSet, booleanTypeSet);
			return booleanTypeSet;
		case B_EQUAL:
		case B_NOT_EQUAL:
			// The following only prevents primitive types from being
			// compared with reference types and different primitive
			// types. However, it is possible to compare references of
			// any type, even if neither is a subtype of the other.
			for (PrimitiveTypeSymbol primitiveType : getTypeSymbols()
					.getPrimitiveTypeSymbols()) {
				ConstraintCondition leftCondition = new ConstraintCondition(
						primitiveType, leftTypeSet);
				ConstraintCondition rightCondition = new ConstraintCondition(
						primitiveType, rightTypeSet);
				ConstantTypeSet primitiveTypeSet = getTypeSetFactory().make(
						primitiveType);
				getSystem().addEquality(rightTypeSet, primitiveTypeSet,
						leftCondition);
				getSystem().addEquality(leftTypeSet, primitiveTypeSet,
						rightCondition);
			}
			return booleanTypeSet;
		case B_LESS_THAN:
		case B_LESS_OR_EQUAL:
		case B_GREATER_THAN:
		case B_GREATER_OR_EQUAL:
			getSystem().addUpperBound(leftTypeSet, numTypeSet);
			getSystem().addEquality(leftTypeSet, rightTypeSet);
			return booleanTypeSet;
		default:
			throw new IllegalStateException("no such binary operator");
		}
	}

	@Override
	public TypeSet methodCall(MethodCallExpr call) {
		return new MethodCallExprConstraintGenerator(this, call).generate();
	}

	@Override
	public TypeSet unaryOp(UnaryOp unaryOp) {
		UOp op = unaryOp.operator;
		TypeSet subExprTypeSet = visit(unaryOp.arg());
		ConstantTypeSet numTypes = getTypeSetFactory().makeNumericalTypeSet();
		ConstantTypeSet booleanType = getTypeSetFactory().makeBoolean();

		switch (op) {
		case U_BOOL_NOT:
			getSystem().addEquality(subExprTypeSet, booleanType);
			return booleanType;
		case U_MINUS:
		case U_PLUS:
			getSystem().addUpperBound(subExprTypeSet, numTypes);
			break;
		}
		return subExprTypeSet;
	}

}