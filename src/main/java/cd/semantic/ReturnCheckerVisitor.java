package cd.semantic;

import cd.ir.ast.Ast;
import cd.ir.ast.IfElse;
import cd.ir.ast.ReturnStmt;
import cd.ir.ast.Seq;
import cd.ir.ast.Stmt;
import cd.ir.ast.WhileLoop;
import cd.ir.AstVisitor;

/**
 * Visitor that checks if all paths of a given sequence have a return statement.
 * 
 * This visitor only needs to be used if are not using the Control Flow Graph.
 * 
 * @author Leo Buttiker
 */
public class ReturnCheckerVisitor extends AstVisitor<Boolean, Void> {

	@Override
	protected Boolean dfltStmt(Stmt ast, Void arg) {
		return false;
	}

	@Override
	public Boolean returnStmt(ReturnStmt ast, Void arg) {
		return true;
	}

	@Override
	public Boolean ifElse(IfElse ast, Void arg) {
		boolean allPathHaveAReturnStmt = true;
		allPathHaveAReturnStmt &= visit(ast.then(), null);
		allPathHaveAReturnStmt &= visit(ast.otherwise(), null);
		return allPathHaveAReturnStmt;
	}

	@Override
	public Boolean seq(Seq ast, Void arg) {

		boolean allPathHaveAReturnStmt = false;
		for (Ast child : ast.children()) {
			allPathHaveAReturnStmt |= this.visit(child, null);
		}
		return allPathHaveAReturnStmt;
	}

	@Override
	public Boolean whileLoop(WhileLoop ast, Void arg) {
		return false;
	}

}