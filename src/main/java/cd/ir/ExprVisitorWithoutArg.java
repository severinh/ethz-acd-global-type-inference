package cd.ir;

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

/**
 * An expression visitor that does offer support for arguments.
 * 
 * @param <R>
 *            the type of return values
 */
public class ExprVisitorWithoutArg<R> extends ExprVisitor<R, Void> {

	public R visit(Expr ast) {
		return ast.accept(this, null);
	}

	protected R dfltExpr(Expr ast) {
		return visitChildren(ast, null);
	}

	@Override
	public R binaryOp(BinaryOp ast, Void arg) {
		return binaryOp(ast);
	}

	public R binaryOp(BinaryOp ast) {
		return dfltExpr(ast);
	}

	@Override
	public R booleanConst(BooleanConst ast, Void arg) {
		return booleanConst(ast);
	}

	public R booleanConst(BooleanConst ast) {
		return dfltExpr(ast);
	}

	@Override
	public R builtInRead(BuiltInRead ast, Void arg) {
		return builtInRead(ast);
	}

	public R builtInRead(BuiltInRead ast) {
		return dfltExpr(ast);
	}

	@Override
	public R builtInReadFloat(BuiltInReadFloat ast, Void arg) {
		return builtInReadFloat(ast);
	}

	public R builtInReadFloat(BuiltInReadFloat ast) {
		return dfltExpr(ast);
	}

	@Override
	public R cast(Cast ast, Void arg) {
		return cast(ast);
	}

	public R cast(Cast ast) {
		return dfltExpr(ast);
	}

	@Override
	public R field(Field ast, Void arg) {
		return field(ast);
	}

	public R field(Field ast) {
		return dfltExpr(ast);
	}

	@Override
	public R index(Index ast, Void arg) {
		return index(ast);
	}

	public R index(Index ast) {
		return dfltExpr(ast);
	}

	@Override
	public R intConst(IntConst ast, Void arg) {
		return intConst(ast);
	}

	public R intConst(IntConst ast) {
		return dfltExpr(ast);
	}

	@Override
	public R floatConst(FloatConst ast, Void arg) {
		return floatConst(ast);
	}

	public R floatConst(FloatConst ast) {
		return dfltExpr(ast);
	}

	@Override
	public R methodCall(MethodCallExpr ast, Void arg) {
		return methodCall(ast);
	}

	public R methodCall(MethodCallExpr ast) {
		return dfltExpr(ast);
	}

	@Override
	public R newObject(NewObject ast, Void arg) {
		return newObject(ast);
	}

	public R newObject(NewObject ast) {
		return dfltExpr(ast);
	}

	@Override
	public R newArray(NewArray ast, Void arg) {
		return newArray(ast);
	}

	public R newArray(NewArray ast) {
		return dfltExpr(ast);
	}

	@Override
	public R nullConst(NullConst ast, Void arg) {
		return nullConst(ast);
	}

	public R nullConst(NullConst ast) {
		return dfltExpr(ast);
	}

	@Override
	public R thisRef(ThisRef ast, Void arg) {
		return thisRef(ast);
	}

	public R thisRef(ThisRef ast) {
		return dfltExpr(ast);
	}

	@Override
	public R unaryOp(UnaryOp ast, Void arg) {
		return unaryOp(ast);
	}

	public R unaryOp(UnaryOp ast) {
		return dfltExpr(ast);
	}

	@Override
	public R var(Var ast, Void arg) {
		return var(ast);
	}

	public R var(Var ast) {
		return dfltExpr(ast);
	}

}
