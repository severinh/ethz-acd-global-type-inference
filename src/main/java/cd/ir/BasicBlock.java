package cd.ir;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cd.ir.Ast.Expr;
import cd.ir.Symbol.VariableSymbol;

/**
 * Node in a control flow graph. New instances should be created via the methods
 * in {@link ControlFlowGraph}. Basic blocks consist of a list of instructions (
 * {@link #instructions}) which are executed at runtime. When the basic block
 * ends, control flows into its {@link #successors}. If the block has more than
 * one successor, it must also have a non-{@code null} value for
 * {@link #condition}, which describes an expression that will determine which
 * successor to take. Basic blocks also have fields for storing the parent and
 * children in the dominator tree. These are generally computed in a second pass
 * once the graph is fully built.
 * 
 * Your team will have to write code that builds the control flow graph and
 * computes the relevant dominator information.
 */
public class BasicBlock {

	/**
	 * Unique numerical index assigned by CFG builder between 0 and the total
	 * number of basic blocks. Useful for indexing into arrays and the like.
	 */
	public final int index;

	/**
	 * List of predecessor blocks in the flow graph (i.e., blocks for which
	 * {@code this} is a successor).
	 */
	public final List<BasicBlock> predecessors = new ArrayList<>();

	/**
	 * List of successor blocks in the flow graph (those that come after the
	 * current block). This list is always either of size 0, 1 or 2: 1 indicates
	 * that control flow continues directly into the next block, and 2 indicates
	 * that control flow goes in one of two directions, depending on the value
	 * that results when {@link #condition} is evaluated at runtime. If there
	 * are two successors, then the 0th entry is taken when {@code condition}
	 * evaluates to {@code true}.
	 * 
	 * @see #trueSuccessor()
	 * @see #falseSuccessor()
	 */
	public final List<BasicBlock> successors = new ArrayList<>();

	/**
	 * List of instructions in this basic block.
	 */
	public final List<Ast> instructions = new ArrayList<>();

	/**
	 * If non-null, indicates that this basic block should have two successors.
	 * Control flows to the first successor if this condition evaluates at
	 * runtime to true, otherwise to the second successor. If null, the basic
	 * block should have only one successor.
	 */
	public Expr condition;

	/**
	 * Parent of this basic block in the dominator tree (initially null until
	 * computed). Otherwise known as the immediate dominator.
	 */
	public BasicBlock dominatorTreeParent = null;

	/**
	 * Children of this basic block in the dominator tree (initially empty until
	 * computed).
	 */
	public final List<BasicBlock> dominatorTreeChildren = new ArrayList<>();

	/**
	 * Contains the dominance frontier of this block. A block b is in the
	 * dominance frontier of another block c if c does not dominate b, but c
	 * DOES dominate a predecessor of b.
	 */
	public final Set<BasicBlock> dominanceFrontier = new HashSet<>();

	/**
	 * Contains any phi nodes attached to this basic block. Phi nodes are keyed
	 * by the symbol from the original program (i.e., version 0 of the symbol
	 * that is their left-hand side).
	 * 
	 * In other words, if a Phi instance {@code P} represents
	 * {@code x3 = phi(x2, x3)}, then {@code x0} would be the key under which
	 * {@code P} would be stored.
	 */
	public final Map<VariableSymbol, Phi> phis = new HashMap<>();

	public BasicBlock(int index) {
		this.index = index;
	}

	public BasicBlock trueSuccessor() {
		assert this.condition != null;
		return this.successors.get(0);
	}

	public BasicBlock falseSuccessor() {
		assert this.condition != null;
		return this.successors.get(1);
	}

	@Override
	public String toString() {
		return "BB" + index;
	}

}
