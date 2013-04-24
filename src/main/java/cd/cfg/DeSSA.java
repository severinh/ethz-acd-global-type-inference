package cd.cfg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cd.Main;
import cd.ir.Ast;
import cd.ir.Ast.Assign;
import cd.ir.Ast.MethodDecl;
import cd.ir.Ast.Var;
import cd.ir.BasicBlock;
import cd.ir.ExprVisitor;
import cd.ir.Phi;
import cd.ir.Symbol.VariableSymbol;
import cd.ir.Symbol.VariableSymbol.Kind;
import cd.util.DepthFirstSearchPreOrder;

public class DeSSA {
	
	public final Main main;
	
	private int maxTemp = 0;
	
	public DeSSA(Main main) {
		this.main = main;
	}
	
	/**
	 * Goes over the control flow graph and removes
	 * any phi nodes, converting SSA variables into normal
	 * variables and phi nodes into standard assignments. 
	 */
	public void compute(final MethodDecl mdecl) {
		for (BasicBlock blk : new DepthFirstSearchPreOrder(mdecl.cfg)) {
			if (blk.phis.isEmpty()) continue; // no phis, no work to do
			
			/**
			 * Build up a dependency graph.  The nodes are variables defined
			 * by phi statements.  The edges represent a copy of the nodes
			 * incoming value, so if a -> b then the value of a is stored into b.
			 * Note that every node in the graph has in-degree at most 1, because
			 * it can only get a value from one location.  Some nodes have in-degree 0
			 * if the value comes from a constant or a variable not created by a phi
			 * statement in this block.
			 * 
			 * To serialize the phi nodes, we walk this graph, forming a spanning
			 * tree.  The value is assigned from the parent in the spanning tree.
			 * If while walking the graph we detect a cycle, we introduce a temporary
			 * to save the original value of one of the nodes in the cycle.  These
			 * temporaries are given a guaranteed unique name like "(de-ssa-0)".
			 */
			
			class DepNode {
				
				/** The node this dependency is defined for. */
				final VariableSymbol originalSym;
				
				/** Originally same as {@code originalSym}.  If we introduce a temp, points to the temp. */
				VariableSymbol tempSym; 
				
				/** Outgoing edges */
				final List<DepNode> copiedToNodes = new ArrayList<DepNode>();
				
				/** Flags used while walking graph */
				boolean onStack, walkedChildren; 
				
				public DepNode(VariableSymbol sym) {
					originalSym = tempSym = sym;
				}
				
				void walkChildren(List<Ast> addToList) {
					if(walkedChildren) // Only walk once.
						return;
					walkedChildren = true;						
					
					onStack = true;
					for(DepNode child : copiedToNodes)
						child.walkAndStore(addToList, this);
					onStack = false;						
				}
				
				void walkAndStore(List<Ast> addToList, DepNode takeValueFrom) {
					if(onStack) {
						// Detected a cycle in the dependency graph.  Introduce
						// a temporary to hold the original value of this 
						// variable, as the original symbol will be overwritten.
						assert tempSym == originalSym;
						tempSym = new VariableSymbol(
								String.format("(de-ssa-%d)", maxTemp++),
								originalSym.type,
								Kind.LOCAL);
						mdecl.sym.locals.put(tempSym.toString(), tempSym);
						addToList.add(new Assign(
								Var.withSym(tempSym),
								Var.withSym(originalSym)));
					} else {
						walkChildren(addToList);
					}
					
					addToList.add(new Assign(
							Var.withSym(originalSym),
							Var.withSym(takeValueFrom.tempSym)));
				}
			}
			
			for (int i = 0; i < blk.predecessors.size(); i++) {
				// Construct nodes in the dependency graph:
				final Map<VariableSymbol, DepNode> depNodes = new HashMap<VariableSymbol, DepNode>(); 
				for (Phi phi : blk.phis.values())
					depNodes.put(phi.lhs, new DepNode(phi.lhs));
				
				// Add edges:
				//   For any assignments to a symbol X whose RHS is not from a phi node
				//   defined in this block, add the assignment to the assigns
				//   array.  These assignments are independent in order from
				//   one another and we will place them at the end.
				//   Note that a variable X may appear as the RHS of other symbols,
				//   and therefore we add the assignment at the end so as to
				//   preserve X's value.
				final List<Assign> assigns = new ArrayList<Assign>();
				for (Phi phi : blk.phis.values()) {
					assert blk.predecessors.size() == phi.rhs.size();
					DepNode lhsNode = depNodes.get(phi.lhs);
					Ast.Expr rhsExpr = phi.rhs.get(i);
					DepNode rhsNode = new ExprVisitor<DepNode, Void>() {
						@Override
						public DepNode var(Var ast, Void arg) {
							return depNodes.get(ast.sym);
						}
					}.visit(rhsExpr, null);
					
					if(rhsNode == null) { // RHS is not a phi variable defined in this block.  No dependencies, 
						Assign assign = new Assign(Var.withSym(phi.lhs), rhsExpr);
						assigns.add(assign);
					} else {
						rhsNode.copiedToNodes.add(lhsNode);
					}
				}
				
				final BasicBlock predblk = blk.predecessors.get(i);					
				assert predblk.successors.size() == 1;
				assert predblk.successors.get(0) == blk;
				
				// Walk the dependency graph, adding
				// assignments for nodes that have incoming edges:
				for(DepNode node : depNodes.values())
					node.walkChildren(predblk.instructions);
				
				// Add other assignments:
				predblk.instructions.addAll(assigns);	
			}				
			
			blk.phis.clear();
		}
	}
		
}
