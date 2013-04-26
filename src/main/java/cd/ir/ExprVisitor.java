package cd.ir;

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

/**
 * A visitor that only visits {@link Expr} nodes.
 */
public class ExprVisitor<R, A> {

	/**
	 * Recurse and process {@code ast}. It is preferred to call this rather than
	 * calling accept directly, since it can be overloaded to introduce
	 * memoization, for example.
	 */
	public R visit(Expr ast, A arg) {
		return ast.accept(this, arg);
	}

	/**
	 * Visits all children of the expression. Relies on the fact that
	 * {@link Expr} nodes only contain other {@link Expr} nodes.
	 */
	public R visitChildren(Expr ast, A arg) {
		R lastValue = null;
		for (Ast child : ast.children())
			lastValue = visit((Expr) child, arg);
		return lastValue;
	}

	/**
	 * The default action for default actions is to call this, which simply
	 * recurses to any children. Also called by seq() by default.
	 */
	protected R dfltExpr(Expr ast, A arg) {
		return visitChildren(ast, arg);
	}

	public R binaryOp(BinaryOp ast, A arg) {
		return dfltExpr(ast, arg);
	}

	public R booleanConst(BooleanConst ast, A arg) {
		return dfltExpr(ast, arg);
	}

	public R builtInRead(BuiltInRead ast, A arg) {
		return dfltExpr(ast, arg);
	}

	public R builtInReadFloat(BuiltInReadFloat ast, A arg) {
		return dfltExpr(ast, arg);
	}

	public R cast(Cast ast, A arg) {
		return dfltExpr(ast, arg);
	}

	public R field(Field ast, A arg) {
		return dfltExpr(ast, arg);
	}

	public R index(Index ast, A arg) {
		return dfltExpr(ast, arg);
	}

	public R intConst(IntConst ast, A arg) {
		return dfltExpr(ast, arg);
	}

	public R floatConst(FloatConst ast, A arg) {
		return dfltExpr(ast, arg);
	}

	public R methodCall(MethodCallExpr ast, A arg) {
		return dfltExpr(ast, arg);
	}

	public R newObject(NewObject ast, A arg) {
		return dfltExpr(ast, arg);
	}

	public R newArray(NewArray ast, A arg) {
		return dfltExpr(ast, arg);
	}

	public R nullConst(NullConst ast, A arg) {
		return dfltExpr(ast, arg);
	}

	public R thisRef(ThisRef ast, A arg) {
		return dfltExpr(ast, arg);
	}

	public R unaryOp(UnaryOp ast, A arg) {
		return dfltExpr(ast, arg);
	}

	public R var(Var ast, A arg) {
		return dfltExpr(ast, arg);
	}

}
