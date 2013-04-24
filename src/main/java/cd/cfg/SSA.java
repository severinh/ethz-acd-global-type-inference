package cd.cfg;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import cd.Main;
import cd.exceptions.SemanticFailure;
import cd.exceptions.SemanticFailure.Cause;
import cd.ir.Ast;
import cd.ir.Ast.Assign;
import cd.ir.Ast.Expr;
import cd.ir.Ast.MethodDecl;
import cd.ir.Ast.Var;
import cd.ir.AstVisitor;
import cd.ir.BasicBlock;
import cd.ir.ControlFlowGraph;
import cd.ir.Phi;
import cd.ir.Symbol;
import cd.ir.Symbol.MethodSymbol;
import cd.ir.Symbol.VariableSymbol;
import cd.util.DepthFirstSearchPreOrder;

public class SSA {

	public final Main main;

	public SSA(Main main) {
		this.main = main;
		uninitSym = new Symbol.VariableSymbol("__UNINIT__", main.nullType);
	}

	private MethodSymbol msym;

	private Map<VariableSymbol, Integer> maxVersions;

	private final VariableSymbol uninitSym;

	public void compute(MethodDecl mdecl) {
		ControlFlowGraph cfg = mdecl.cfg;

		main.debug("Computing SSA form for %s", mdecl.name);

		// Phase 1: introduce Phis
		for (BasicBlock bb : cfg.allBlocks) {
			main.debug("  Adding Phis for %s of %s", bb, mdecl.name);

			// Compute iterated dominance frontier for this block 'bb'.
			Set<BasicBlock> idf = new HashSet<BasicBlock>();
			computeIteratedDominanceFrontier(bb, idf);
			main.debug("    df=%s", bb.dominanceFrontier);
			main.debug("    idf=%s", idf);

			// Introduce phi blocks.
			for (Ast ast : bb.instructions)
				new IntroducePhiVisitor().visit(ast, idf);
		}
		// Phase 2: Renumber
		msym = mdecl.sym;
		maxVersions = new HashMap<VariableSymbol, Integer>();
		Map<VariableSymbol, VariableSymbol> currentVersions = new HashMap<VariableSymbol, VariableSymbol>();
		for (VariableSymbol sym : mdecl.sym.parameters)
			currentVersions.put(sym, sym);
		for (VariableSymbol sym : mdecl.sym.locals.values())
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
				VariableSymbol sym = var.sym;
				addPhis(sym, idf);
			}
			return super.assign(ast, idf);
		}

		private void addPhis(VariableSymbol sym, Set<BasicBlock> idf) {
			main.debug("Introducing phis for %s at %s", sym, idf);
			for (BasicBlock bb : idf) {
				if (bb.phis.containsKey(sym))
					continue; // already has a phi for this symbol
				Phi phi = new Phi(sym, bb.predecessors.size());
				bb.phis.put(sym, phi);
				main.debug("  New phi created %s at %s", phi, bb);
			}
		}

	}

	private void renumberBlock(BasicBlock block,
			Map<VariableSymbol, VariableSymbol> inCurrentVersions) {
		Map<VariableSymbol, VariableSymbol> currentVersions = new HashMap<VariableSymbol, VariableSymbol>(
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
				phi.rhs.set(predIndex, Ast.Var.withSym(cursym));
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
				if (lhs instanceof Ast.Var) {
					Var var = (Var) lhs;
					var.setSymbol(renumberDefinedSymbol(var.sym,
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
				assert currentVersions.containsKey(ast.sym);
				ast.sym = currentVersions.get(ast.sym);
				ast.name = ast.sym.toString();
				return null;
			}

		}.visit(ast, null);
	}

	private VariableSymbol renumberDefinedSymbol(VariableSymbol lhs,
			Map<VariableSymbol, VariableSymbol> currentVersions) {
		assert lhs.version == 0; // this should be a v0sym
		if (!maxVersions.containsKey(lhs))
			maxVersions.put(lhs, 0);
		int version = maxVersions.get(lhs) + 1;
		maxVersions.put(lhs, version);
		VariableSymbol sym = new VariableSymbol(lhs, version);
		msym.locals.put(sym.toString(), sym);
		currentVersions.put(lhs, sym);
		return sym;
	}

	private void findUninitialized(ControlFlowGraph cfg) {
		// Construct a map from each var defined in a phi
		// to the set of symbols whose value it may take on.
		final Map<VariableSymbol, Phi> phiDefs = new HashMap<VariableSymbol, Phi>();
		for (BasicBlock blk : new DepthFirstSearchPreOrder(cfg))
			for (Phi phi : blk.phis.values())
				phiDefs.put(phi.lhs, phi);

		// Walk the basic blocks and try to find uses of
		// uninitialized variables.
		final Map<VariableSymbol, Set<VariableSymbol>> expansions = new HashMap<VariableSymbol, Set<VariableSymbol>>();
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
							expandInto(((Ast.Var) rhs).sym, result);
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
					result = new HashSet<VariableSymbol>();
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
				VariableSymbol sym = ast.sym;
				Set<VariableSymbol> syms = expand(sym);
				if (syms.contains(uninitSym))
					throw new SemanticFailure(Cause.POSSIBLY_UNINITIALIZED,
							"Variable %s may be used uninitialized!", ast.name);
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
