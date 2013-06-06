package cd.ir.ast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cd.debug.AstOneLine;
import cd.ir.AstVisitor;

public abstract class Ast {

	/**
	 * The list of children AST nodes. Typically, this list is of a fixed size:
	 * its contents can also be accessed using the various accessors defined on
	 * the Ast subtypes.
	 * 
	 * <p>
	 * <b>Note:</b> this list may contain null pointers!
	 */
	protected final List<Ast> rwChildren;

	protected Ast(int fixedCount) {
		if (fixedCount == -1)
			this.rwChildren = new ArrayList<>();
		else
			this.rwChildren = Arrays.asList(new Ast[fixedCount]);
	}

	/**
	 * Returns a copy of the list of children for this node. The result will
	 * never contain null pointers.
	 */
	public List<Ast> children() {
		ArrayList<Ast> result = new ArrayList<>();
		for (Ast n : rwChildren) {
			if (n != null)
				result.add(n);
		}
		return result;
	}
	
	public void removeChild(Ast child) {
		rwChildren.remove(child);
	}

	/**
	 * Returns a new list containing all children AST nodes that are of the
	 * given type.
	 */
	public <A> List<A> childrenOfType(Class<A> C) {
		List<A> res = new ArrayList<>();
		for (Ast c : children()) {
			if (C.isInstance(c))
				res.add(C.cast(c));
		}
		return res;
	}

	/** Accept method for the pattern Visitor. */
	public abstract <R, A> R accept(AstVisitor<R, A> visitor, A arg);

	/** Makes a deep clone of this AST node. */
	public abstract Ast deepCopy();

	/** Convenient debugging printout */
	@Override
	public String toString() {
		return String.format("(%s)@%x", AstOneLine.toString(this),
				System.identityHashCode(this));
	}

}
