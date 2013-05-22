package cd.ir;

import cd.ir.ast.Assign;
import cd.ir.ast.Ast;
import cd.ir.ast.BuiltInTick;
import cd.ir.ast.BuiltInTock;
import cd.ir.ast.BuiltInWrite;
import cd.ir.ast.BuiltInWriteFloat;
import cd.ir.ast.BuiltInWriteln;
import cd.ir.ast.ClassDecl;
import cd.ir.ast.Decl;
import cd.ir.ast.Expr;
import cd.ir.ast.IfElse;
import cd.ir.ast.MethodCall;
import cd.ir.ast.MethodDecl;
import cd.ir.ast.Nop;
import cd.ir.ast.ReturnStmt;
import cd.ir.ast.Seq;
import cd.ir.ast.Stmt;
import cd.ir.ast.VarDecl;
import cd.ir.ast.WhileLoop;

/** A visitor that visits any kind of node */
public class AstVisitor<R, A> extends ExprVisitor<R, A> {

	/**
	 * Recurse and process {@code ast}. It is preferred to call this rather than
	 * calling accept directly, since it can be overloaded to introduce
	 * memoization, for example.
	 */
	public R visit(Ast ast, A arg) {
		return ast.accept(this, arg);
	}

	/**
	 * A handy function which visits the children of {@code ast}, providing
	 * "arg" to each of them. It returns the result of the last child in the
	 * list. It is invoked by the method {@link #dflt(Ast, Object)} by default.
	 */
	public R visitChildren(Ast ast, A arg) {
		R lastValue = null;
		for (Ast child : ast.children())
			lastValue = visit(child, arg);
		return lastValue;
	}

	/**
	 * The default action for default actions is to call this, which simply
	 * recurses to any children. Also called by seq() by default.
	 */
	protected R dflt(Ast ast, A arg) {
		return visitChildren(ast, arg);
	}

	/**
	 * The default action for statements is to call this
	 */
	protected R dfltStmt(Stmt ast, A arg) {
		return dflt(ast, arg);
	}

	/**
	 * The default action for expressions is to call this
	 */
	@Override
	protected R dfltExpr(Expr ast, A arg) {
		return dflt(ast, arg);
	}

	/**
	 * The default action for AST nodes representing declarations is to call
	 * this function
	 */
	protected R dfltDecl(Decl ast, A arg) {
		return dflt(ast, arg);
	}

	public R assign(Assign ast, A arg) {
		return dfltStmt(ast, arg);
	}

	public R builtInWrite(BuiltInWrite ast, A arg) {
		return dfltStmt(ast, arg);
	}

	public R builtInWriteFloat(BuiltInWriteFloat ast, A arg) {
		return dfltStmt(ast, arg);
	}

	public R builtInWriteln(BuiltInWriteln ast, A arg) {
		return dfltStmt(ast, arg);
	}
	
	public R builtInTick(BuiltInTick ast, A arg) {
		return dfltStmt(ast, arg);
	}
	
	public R builtInTock(BuiltInTock ast, A arg) {
		return dfltStmt(ast, arg);
	}

	public R classDecl(ClassDecl ast, A arg) {
		return dfltDecl(ast, arg);
	}

	public R methodDecl(MethodDecl ast, A arg) {
		return dfltDecl(ast, arg);
	}

	public R varDecl(VarDecl ast, A arg) {
		return dfltDecl(ast, arg);
	}

	public R ifElse(IfElse ast, A arg) {
		return dfltStmt(ast, arg);
	}

	public R returnStmt(ReturnStmt ast, A arg) {
		return dfltStmt(ast, arg);
	}

	public R methodCall(MethodCall ast, A arg) {
		return dfltStmt(ast, arg);
	}

	public R nop(Nop ast, A arg) {
		return dfltStmt(ast, arg);
	}

	public R seq(Seq ast, A arg) {
		return dflt(ast, arg);
	}

	public R whileLoop(WhileLoop ast, A arg) {
		return dfltStmt(ast, arg);
	}
}
