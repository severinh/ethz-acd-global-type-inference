package cd.ir.ast;

import java.util.ArrayList;
import java.util.List;

import cd.ir.AstVisitor;

/**
 * Used in {@link MethodDecl} to group together declarations and method bodies.
 */
public class Seq extends Decl {

	public Seq(List<Ast> nodes) {
		super(-1);
		if (nodes != null)
			this.rwChildren.addAll(nodes);
	}

	/** Grant access to the raw list of children for seq nodes */
	public List<Ast> rwChildren() {
		return this.rwChildren;
	}

	@Override
	public <R, A> R accept(AstVisitor<R, A> visitor, A arg) {
		return visitor.seq(this, arg);
	}

	@Override
	public Ast deepCopy() {
		List<Ast> result = new ArrayList<>();

		for (final Ast ast : this.rwChildren) {
			result.add(ast.deepCopy());
		}

		return new Seq(result);
	}

}