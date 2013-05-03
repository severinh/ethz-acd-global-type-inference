package cd.cfg;

import cd.ir.AstVisitor;
import cd.ir.BasicBlock;
import cd.ir.ControlFlowGraph;
import cd.ir.ast.Ast;
import cd.ir.ast.IfElse;
import cd.ir.ast.MethodDecl;
import cd.ir.ast.ReturnStmt;
import cd.ir.ast.Seq;
import cd.ir.ast.Stmt;
import cd.ir.ast.WhileLoop;

public class CFGBuilder {

	private ControlFlowGraph cfg;

	public void build(MethodDecl mdecl) {
		cfg = mdecl.cfg = new ControlFlowGraph();
		BasicBlock lastInBody = new Visitor().visit(mdecl.body(), cfg.start);
		if (lastInBody != null)
			cfg.connect(lastInBody, cfg.end);
	}

	protected class Visitor extends AstVisitor<BasicBlock, BasicBlock> {

		@Override
		protected BasicBlock dfltStmt(Stmt ast, BasicBlock arg) {
			if (arg == null)
				return null; // dead code, no need to generate anything
			arg.instructions.add(ast);
			return arg;
		}

		@Override
		public BasicBlock ifElse(IfElse ast, BasicBlock arg) {
			cfg.terminateInCondition(arg, ast.condition());
			BasicBlock then = visit(ast.then(), arg.trueSuccessor());
			BasicBlock otherwise = visit(ast.otherwise(), arg.falseSuccessor());
			if (then != null && otherwise != null)
				return cfg.join(then, otherwise);
			else if (then != null)
				return then;
			else if (otherwise != null)
				return otherwise;
			else
				return null;
		}

		@Override
		public BasicBlock seq(Seq ast, BasicBlock arg) {
			for (Ast child : ast.children())
				arg = this.visit(child, arg);
			return arg;
		}

		@Override
		public BasicBlock whileLoop(WhileLoop ast, BasicBlock arg) {
			BasicBlock cond = cfg.join(arg);
			cfg.terminateInCondition(cond, ast.condition());
			BasicBlock body = visit(ast.body(), cond.trueSuccessor());
			if (body != null)
				cfg.connect(body, cond);
			return cond.falseSuccessor();
		}

		@Override
		public BasicBlock returnStmt(ReturnStmt ast, BasicBlock arg) {
			if (arg == null)
				return null; // dead code, no need to generate anything
			arg.instructions.add(ast);
			cfg.connect(arg, cfg.end);
			return null; // null means that this block leads nowhere else
		}

	}

}
