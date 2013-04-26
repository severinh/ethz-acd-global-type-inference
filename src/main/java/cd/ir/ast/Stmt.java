package cd.ir.ast;

/** Interface for all statements */
public abstract class Stmt extends Ast {

	protected Stmt(int fixedCount) {
		super(fixedCount);
	}

}