package cd.cfg;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cd.Main;
import cd.exceptions.SemanticFailure;
import cd.exceptions.SemanticFailure.Cause;
import cd.ir.ast.Assign;
import cd.ir.ast.Ast;
import cd.ir.ast.Expr;
import cd.ir.ast.MethodDecl;
import cd.ir.ast.Var;
import cd.ir.symbols.MethodSymbol;
import cd.ir.symbols.VariableSymbol;
import cd.ir.AstVisitor;
import cd.ir.BasicBlock;
import cd.ir.ControlFlowGraph;
import cd.ir.Phi;
import cd.util.DepthFirstSearchPreOrder;

public class SSA {

	private static final Logger LOG = LoggerFactory.getLogger(SSA.class);

	private final VariableSymbol uninitSym;

	public SSA(Main main) {
		this.uninitSym = new VariableSymbol("__UNINIT__",
				main.typeSymbols.getNullType());
	}

	private MethodSymbol msym;
	private Map<VariableSymbol, Integer> maxVersions;

	public void compute(MethodDecl mdecl) {
		ControlFlowGraph cfg = mdecl.cfg;

		LOG.debug("Computing SSA form for {}", mdecl.name);

		// Phase 1: introduce Phis
		for (BasicBlock bb : cfg.allBlocks) {
			LOG.debug("  Adding Phis for {} of {}", bb, mdecl.name);

			// Compute iterated dominance frontier for this block 'bb'.
			Set<BasicBlock> idf = new HashSet<>();
			computeIteratedDominanceFrontier(bb, idf);
			LOG.debug("    df={}", bb.dominanceFrontier);
			LOG.debug("    idf={}", idf);

			// Introduce phi blocks.
			for (Ast ast : bb.instructions)
				new IntroducePhiVisitor().visit(ast, idf);
		}
		// Phase 2: Renumber
		msym = mdecl.sym;
		maxVersions = new HashMap<>();
		Map<VariableSymbol, VariableSymbol> currentVersions = new HashMap<>();
		for (VariableSymbol sym : mdecl.sym.getParameters())
			currentVersions.put(sym, sym);
		for (VariableSymbol sym : mdecl.sym.getLocals())
			currentVersions.put(sym, uninitSym);
		renumberBlock(cfg.start, currentVersions);
		// Phase 3: Detect uses of (potentially) uninitialized variables
		findUninitialized(cfg);
	}

	public void computeIteratedDominanceFrontier(BasicBlock bb,
			Set<BasicBlock> idf) {
		for (BasicBlock b : bb.dominanceFrontier) {
			if (idf.add(b))
				computeIteratedDominanceFrontier(b, idf);
		}
	}

	class IntroducePhiVisitor extends AstVisitor<Void, Set<BasicBlock>> {

		@Override
		public Void assign(Assign ast, Set<BasicBlock> idf) {
			Expr lhs = ast.left();
			if (lhs instanceof Var) {
				Var var = (Var) lhs;
				VariableSymbol sym = var.getSymbol();
				addPhis(sym, idf);
			}
			return super.assign(ast, idf);
		}

		private void addPhis(VariableSymbol sym, Set<BasicBlock> idf) {
			LOG.debug("Introducing phis for {} at {}", sym, idf);
			for (BasicBlock bb : idf) {
				if (bb.phis.containsKey(sym))
					continue; // already has a phi for this symbol
				Phi phi = new Phi(sym, bb.predecessors.size());
				bb.phis.put(sym, phi);
				LOG.debug("  New phi created {} at {}", phi, bb);
			}
		}

	}

	private void renumberBlock(BasicBlock block,
			Map<VariableSymbol, VariableSymbol> inCurrentVersions) {
		Map<VariableSymbol, VariableSymbol> currentVersions = new HashMap<>(
				inCurrentVersions);

		for (Phi phi : block.phis.values()) {
			assert phi.v0sym == phi.lhs;
			phi.lhs = renumberDefinedSymbol(phi.lhs, currentVersions);
		}

		for (Ast ast : block.instructions)
			renumberAST(ast, currentVersions);

		if (block.condition != null)
			renumberAST(block.condition, currentVersions);

		for (BasicBlock succ : block.successors) {
			int predIndex = succ.predecessors.indexOf(block);
			for (Phi phi : succ.phis.values()) {
				VariableSymbol cursym = currentVersions.get(phi.v0sym);
				assert cursym != null;
				phi.rhs.set(predIndex, Var.withSym(cursym));
			}
		}

		for (BasicBlock dblock : block.dominatorTreeChildren) {
			renumberBlock(dblock, currentVersions);
		}
	}

	/**
	 * Rewrites an instruction/expression from within a block so that all
	 * references to variables use the most recent versions of that variable. If
	 * there are any assignments, then updates the currentVersions table with a
	 * new version for the variable.
	 */
	private void renumberAST(Ast ast,
			final Map<VariableSymbol, VariableSymbol> currentVersions) {
		new AstVisitor<Void, Void>() {

			/**
			 * This method is reached when we encounter a potential DEFINITION
			 * of a variable. Therefore, it defines a new version of that
			 * variable and updates the currentVersions table.
			 */
			@Override
			public Void assign(Assign ast, Void arg) {
				renumberAST(ast.right(), currentVersions);

				Ast lhs = ast.left();
				if (lhs instanceof Var) {
					Var var = (Var) lhs;
					var.setSymbol(renumberDefinedSymbol(var.getSymbol(),
							currentVersions));
				} else {
					renumberAST(ast.left(), currentVersions);
				}
				return null;
			}

			/**
			 * This method is reached when we encounter a USE of a variable. It
			 * rewrites the variable to use whatever version is current.
			 */
			@Override
			public Void var(Var ast, Void arg) {
				assert currentVersions.containsKey(ast.getSymbol());
				ast.setSymbol(currentVersions.get(ast.getSymbol()));
				ast.setName(ast.getSymbol().toString());
				return null;
			}

		}.visit(ast, null);
	}

	private VariableSymbol renumberDefinedSymbol(VariableSymbol lhs,
			Map<VariableSymbol, VariableSymbol> currentVersions) {
		assert lhs.getVersion() == 0; // this should be a v0sym
		if (!maxVersions.containsKey(lhs))
			maxVersions.put(lhs, 0);
		int version = maxVersions.get(lhs) + 1;
		maxVersions.put(lhs, version);
		VariableSymbol sym = new VariableSymbol(lhs, version);
		msym.addLocal(sym);
		currentVersions.put(lhs, sym);
		return sym;
	}

	private void findUninitialized(ControlFlowGraph cfg) {
		// Construct a map from each var defined in a phi
		// to the set of symbols whose value it may take on.
		final Map<VariableSymbol, Phi> phiDefs = new HashMap<>();
		for (BasicBlock blk : new DepthFirstSearchPreOrder(cfg))
			for (Phi phi : blk.phis.values())
				phiDefs.put(phi.lhs, phi);

		// Walk the basic blocks and try to find uses of
		// uninitialized variables.
		final Map<VariableSymbol, Set<VariableSymbol>> expansions = new HashMap<>();
		AstVisitor<Void, Void> uninitVisitor = new AstVisitor<Void, Void>() {

			/** Helper for {@code expand()}. */
			private void expandInto(VariableSymbol sym,
					Set<VariableSymbol> result) {
				if (expansions.containsKey(sym))
					result.addAll(expansions.get(sym));
				else if (result.add(sym)) {
					Phi phi = phiDefs.get(sym);
					if (phi != null)
						for (Expr rhs : phi.rhs)
							expandInto(((Var) rhs).getSymbol(), result);
				}
			}

			/**
			 * Given a variable {@code sym}, result a set of symbols whose value
			 * it might take on. If {@code sym} is not defined by a phi node,
			 * this will be a singleton set containing {@code sym}.
			 */
			private Set<VariableSymbol> expand(VariableSymbol sym) {
				Set<VariableSymbol> result = expansions.get(sym);
				if (result == null) {
					result = new HashSet<>();
					expandInto(sym, result);
					expansions.put(sym, result);
				}
				return result;
			}

			/**
			 * For each use of var, find all the variable symbols whose value it
			 * might have, and ensure that {@code uninitSym} is not among them.
			 * Note: this code gets triggered for var definitions as well, but
			 * it's harmless.
			 */
			@Override
			public Void var(Var ast, Void arg) {
				VariableSymbol sym = ast.getSymbol();
				Set<VariableSymbol> syms = expand(sym);
				if (syms.contains(uninitSym))
					throw new SemanticFailure(Cause.POSSIBLY_UNINITIALIZED,
							"Variable %s may be used uninitialized!",
							ast.getName());
				return null;
			}
		};

		for (BasicBlock blk : new DepthFirstSearchPreOrder(cfg)) {
			for (Ast ast : blk.instructions)
				uninitVisitor.visit(ast, null);
			if (blk.condition != null)
				uninitVisitor.visit(blk.condition, null);
		}
	}

}
