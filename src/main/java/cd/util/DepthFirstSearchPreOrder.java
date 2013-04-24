package cd.util;

import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Stack;

import cd.ir.BasicBlock;
import cd.ir.ControlFlowGraph;

/**
 * A potentially handy iterator which yields the blocks in a control-flow
 * graph.  The order is pre-order, depth-first.  Pre-order means that a
 * node is visited before its successors. 
 */
public class DepthFirstSearchPreOrder implements Iterable<BasicBlock> {
	
	public final ControlFlowGraph cfg;
	
	public DepthFirstSearchPreOrder(ControlFlowGraph cfg) {
		this.cfg = cfg;
	}

	public Iterator<BasicBlock> iterator() {
		return new Iterator<BasicBlock>() {
			
			/** Blocks we still need to visit */
			private final Stack<BasicBlock> stack = new Stack<BasicBlock>();
			
			/** Blocks we pushed thus far */
			private final Set<BasicBlock> pushed = new HashSet<BasicBlock>();

			{
				stack.add(cfg.start);
				pushed.add(cfg.start);
			}

			public boolean hasNext() {
				return !stack.isEmpty();
			}

			public BasicBlock next() {
				if (stack.isEmpty())
					throw new NoSuchElementException();
				
				BasicBlock res = stack.pop();
				for (BasicBlock s : res.successors)
					if (!pushed.contains(s)) {
						pushed.add(s);
						stack.add(s);
					}
				
				return res;
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}
			
		};
	}

}
