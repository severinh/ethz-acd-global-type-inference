package cd.codegen;

import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cd.Main;
import cd.ir.Ast;
import cd.ir.Ast.ClassDecl;
import cd.ir.Ast.MethodDecl;
import cd.ir.AstVisitor;
import cd.ir.BasicBlock;
import cd.ir.ControlFlowGraph;

public class CfgCodeGenerator {

	private final Main main;
	private final AstCodeGenerator cg;

	public CfgCodeGenerator(Main main, Writer out) {
		this.main = main;
		this.cg = new AstCodeGenerator(main, out);
	}

	public void debug(String format, Object... args) {
		this.main.debug(format, args);
	}

	public void go(List<? extends ClassDecl> astRoots) {
		cg.emitPrefix(astRoots);
		for (ClassDecl cdecl : astRoots)
			new CfgStmtVisitor().visit(cdecl, null);
	}

	public class CfgStmtVisitor extends AstVisitor<Void, Void> {

		@Override
		public Void classDecl(ClassDecl ast, Void arg) {
			cg.emitCommentSection("Class " + ast.name);
			cg.emitIndent("");
			super.classDecl(ast, arg);
			cg.emitUndent();
			return null;
		}

		@Override
		public Void methodDecl(MethodDecl ast, Void arg) {
			cg.emitMethodPrefix(ast);

			ControlFlowGraph cfg = ast.cfg;
			assert cfg != null;

			Map<BasicBlock, String> labels = new HashMap<>();
			for (BasicBlock blk : cfg.allBlocks)
				labels.put(blk, cg.uniqueLabel());
			String exitLabel = cg.uniqueLabel();

			cg.emit("jmp", labels.get(cfg.start));

			for (BasicBlock blk : cfg.allBlocks) {

				cg.emitCommentSection("Basic block " + blk.index);
				cg.emitLabel(labels.get(blk));

				for (Ast instr : blk.instructions)
					cg.sdg.gen(instr);

				if (blk == cfg.end) {
					cg.emitComment(String.format("Return"));
					assert blk.successors.size() == 0;
					cg.emit("jmp", exitLabel);
				} else if (blk.condition != null) {
					assert blk.successors.size() == 2;
					cg.emitComment(String.format(
							"Exit to block %d if true, block %d if false",
							blk.trueSuccessor().index,
							blk.falseSuccessor().index));
					cg.genJumpIfFalse(blk.condition,
							labels.get(blk.falseSuccessor()));
					cg.emit("jmp", labels.get(blk.trueSuccessor()));
				} else {
					cg.emitComment(String.format("Exit to block %d",
							blk.successors.get(0).index));
					assert blk.successors.size() == 1;
					cg.emit("jmp", labels.get(blk.successors.get(0)));
				}
			}

			cg.emitLabel(exitLabel);
			if (ast.sym.returnType.equals(main.voidType)) {
				cg.emitMethodSuffix(true);
			} else {
				cg.emitMethodSuffix(true);
			}
			return null;
		}

	}

}
