package cd.cfg;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;

import cd.Main;
import cd.ir.BasicBlock;
import cd.ir.ControlFlowGraph;

/**
 * Computes dominators and dominator tree of a control-flow graph.
 */
public class Dominator {

	public final Main main;

	public Dominator(Main main) {
		this.main = main;
	}

	private List<BasicBlock> blocksInRevPostOrder;
	private int[] postOrderIndex;

	public void compute(ControlFlowGraph cfg) {
		// Compute the post order information for the control flow graph
		blocksInRevPostOrder = new ArrayList<>();
		postOrderIndex = new int[cfg.allBlocks.size()];
		depthFirstSearch(cfg.start, new BitSet());
		Collections.reverse(blocksInRevPostOrder);
		// Iteratively improve immediate dominator
		boolean changed = true;
		while (changed) {
			changed = false;
			for (BasicBlock blk : blocksInRevPostOrder) {
				BasicBlock dom = null;

				for (BasicBlock pblk : blk.predecessors) {
					if (dom == null)
						dom = pblk;
					else if (pblk == cfg.start
							|| pblk.dominatorTreeParent != null)
						// if dominator of our pred is initialized
						dom = intersect(dom, pblk);
				}

				if (dom != blk.dominatorTreeParent) {
					blk.dominatorTreeParent = dom;
					changed = true;
				}
			}
		}
		// Compute children in dominator tree
		for (BasicBlock blk : cfg.allBlocks)
			if (blk.dominatorTreeParent != null)
				blk.dominatorTreeParent.dominatorTreeChildren.add(blk);
		// Compute dominance frontier
		for (BasicBlock blk : cfg.allBlocks) {
			main.debug("blk=%s preds=%s", blk, blk.predecessors);

			for (BasicBlock pblk : blk.predecessors) {
				main.debug("blk=%s(%s) pblk=%s", blk, blk.dominatorTreeParent,
						pblk);
				BasicBlock runner = pblk;
				while (runner != blk.dominatorTreeParent && runner != null) {
					runner.dominanceFrontier.add(blk);
					runner = runner.dominatorTreeParent;
				}
			}
		}
	}

	private BasicBlock intersect(BasicBlock dom, BasicBlock pblk) {
		assert dom != null && pblk != null;
		BasicBlock finger1 = dom;
		BasicBlock finger2 = pblk;
		while (finger1 != finger2) {
			while (postOrderIndex[finger1.index] < postOrderIndex[finger2.index])
				finger1 = finger1.dominatorTreeParent;
			while (postOrderIndex[finger2.index] < postOrderIndex[finger1.index])
				finger2 = finger2.dominatorTreeParent;
		}
		return finger1;
	}

	public void depthFirstSearch(BasicBlock blk, BitSet visited) {
		// Did we visit blk already?
		if (visited.get(blk.index))
			return;
		visited.set(blk.index);

		// Visit blk's successors
		for (BasicBlock cblk : blk.successors)
			depthFirstSearch(cblk, visited);

		// Add blk to post order array
		postOrderIndex[blk.index] = blocksInRevPostOrder.size();
		blocksInRevPostOrder.add(blk); // initially in post order, but gets
										// reversed later
	}

}
