package cd.cfg;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cd.Main;
import cd.debug.AstOneLine;
import cd.debug.CfgDump;
import cd.ir.ast.Assign;
import cd.ir.ast.Ast;
import cd.ir.ast.AstRewriteVisitor;
import cd.ir.ast.BinaryOp;
import cd.ir.ast.BooleanConst;
import cd.ir.ast.Cast;
import cd.ir.ast.Expr;
import cd.ir.ast.IntConst;
import cd.ir.ast.MethodDecl;
import cd.ir.ast.NullConst;
import cd.ir.ast.ThisRef;
import cd.ir.ast.UnaryOp;
import cd.ir.ast.Var;
import cd.ir.symbols.MethodSymbol;
import cd.ir.symbols.VariableSymbol;
import cd.ir.AstVisitor;
import cd.ir.BasicBlock;
import cd.ir.ExprVisitor;
import cd.ir.Phi;
import cd.util.DepthFirstSearchPreOrder;

public class Optimizer {

	private static final Logger LOG = LoggerFactory.getLogger(Optimizer.class);

	private static final boolean INTENSE_DEBUG = true;
	private static final int MAX_INNER = 16, MAX_OUTER = 16;

	private final Main main;
	private int changes = 0;

	private static int overall;

	public Optimizer(Main main) {
		this.main = main;
	}

	private String phase() {
		return format(".opt.%d", overall++);
	}

	public void compute(MethodDecl md) {
		int oldChanges, outer = 0;
		do {

			if (INTENSE_DEBUG)
				CfgDump.toString(md, phase() + ".before", main.cfgdumpbase,
						false);
			LOG.debug("Before phase = {}", overall - 1);

			int inner = 0;
			do {
				oldChanges = changes;

				LOG.debug("Constant Prop, phase {}", overall);
				new ConstantPropagation().compute(md);
				if (INTENSE_DEBUG)
					CfgDump.toString(md, phase() + ".constant",
							main.cfgdumpbase, false);

				LOG.debug("Copy Prop, phase {}", overall);
				new CopyPropagation().compute(md);
				if (INTENSE_DEBUG)
					CfgDump.toString(md, phase() + ".copyprop",
							main.cfgdumpbase, false);
			} while (changes > oldChanges && inner++ < MAX_INNER);

			LOG.debug("CSE, phase {}", overall);
			new CommonSubexpressionElimination().compute(md);
			if (INTENSE_DEBUG)
				CfgDump.toString(md, phase() + ".cse", main.cfgdumpbase, false);

		} while (changes > oldChanges && outer++ < MAX_OUTER);
	}

	private class ConstantPropagation extends AstVisitor<Void, Void> {

		@SuppressWarnings("serial")
		public class NotConstantException extends Exception {
		}

		public Ast rewrite(Ast ast) {
			if (ast != null)
				return new AstRewriter().visit(ast, null);
			else
				return null;
		}

		public class AstRewriter extends AstRewriteVisitor<Void> {

			private int asInt(Ast ast) throws NotConstantException {
				if (ast instanceof IntConst)
					return ((IntConst) ast).value;

				// Hack to make == and != work. Note that semantic
				// check already guarantees booleans don't appear
				// where they are not permitted.
				if (ast instanceof BooleanConst)
					return (((BooleanConst) ast).value ? 1 : 0);

				throw new NotConstantException();
			}

			private boolean asBool(Ast ast) throws NotConstantException {
				if (ast instanceof BooleanConst)
					return ((BooleanConst) ast).value;
				throw new NotConstantException();
			}

			@Override
			public Ast unaryOp(UnaryOp ast, Void dummy) {
				// Rewrite and replace our child:
				super.unaryOp(ast, dummy);
				try {
					Integer intValue = null;
					Boolean boolValue = null;

					switch (ast.operator) {
					case U_BOOL_NOT:
						boolValue = !asBool(ast.arg());
						break;
					case U_MINUS:
						intValue = -asInt(ast.arg());
						break;
					case U_PLUS:
						intValue = asInt(ast.arg());
						break;
					}

					return replace(ast, intValue, boolValue);
				} catch (NotConstantException e) {
					// non-constant argument, no effect
					return ast;
				}
			}

			@Override
			public Ast binaryOp(BinaryOp ast, Void arg) {

				// Rewrite and replace our children:
				super.binaryOp(ast, arg);

				try {
					Expr left = ast.left();
					Expr right = ast.right();
					Integer intValue = null;
					Boolean boolValue = null;

					switch (ast.operator) {
					case B_TIMES:
						intValue = asInt(left) * asInt(right);
						break;
					case B_DIV:
						intValue = asInt(left) / asInt(right);
						break;
					case B_MOD:
						intValue = asInt(left) % asInt(right);
						break;
					case B_PLUS:
						intValue = asInt(left) + asInt(right);
						break;
					case B_MINUS:
						intValue = asInt(left) - asInt(right);
						break;
					case B_AND:
						boolValue = asBool(left) && asBool(right);
						break;
					case B_OR:
						boolValue = asBool(left) || asBool(right);
						break;
					case B_LESS_THAN:
						boolValue = asInt(left) < asInt(right);
						break;
					case B_LESS_OR_EQUAL:
						boolValue = asInt(left) <= asInt(right);
						break;
					case B_GREATER_THAN:
						boolValue = asInt(left) > asInt(right);
						break;
					case B_GREATER_OR_EQUAL:
						boolValue = asInt(left) >= asInt(right);
						break;
					case B_EQUAL:
						boolValue = areEqual(left, right);
						break;
					case B_NOT_EQUAL:
						boolValue = areEqual(left, right);
						if (boolValue != null)
							boolValue = !boolValue;
						break;
					}

					return replace(ast, intValue, boolValue);
				} catch (NotConstantException exc) {
					// non-constant operands: make no change.
					return ast;
				} catch (ArithmeticException exc) {
					// division by zero etc: make no change.
					return ast;
				}
			}

			private Boolean areEqual(Expr left, Expr right)
					throws NotConstantException {
				Boolean boolValue;
				if (left.type == main.typeSymbols.getIntType())
					boolValue = asInt(left) == asInt(right);
				else if (left.type == main.typeSymbols.getBooleanType())
					boolValue = asBool(left) == asBool(right);
				else
					boolValue = (isNull(left) && isNull(right) ? true : null);
				return boolValue;
			}

			private boolean isNull(Expr left) {
				return (left instanceof NullConst);
			}

			private Ast replace(Expr original, Integer intValue,
					Boolean boolValue) {
				Expr replacement = null;
				if (intValue != null)
					replacement = new IntConst(intValue);
				else if (boolValue != null)
					replacement = new BooleanConst(boolValue);

				if (replacement != null) {
					// type of the expression does not change:
					replacement.type = original.type;
					changes++;
					LOG.debug("Constant Prop: Replacing {} with {}", original,
							replacement);
					return replacement;
				}

				return original;
			}

			@Override
			public Ast var(Var ast, Void arg) {
				// Constants are already replaced in semantic
				// phase by ExprRewriter, but otherwise we could do so here.
				return super.var(ast, arg);
			}

		}

		public void compute(MethodDecl md) {
			for (BasicBlock blk : new DepthFirstSearchPreOrder(md.cfg)) {
				for (int i = 0; i < blk.instructions.size(); i++) {
					Ast instruction = blk.instructions.get(i);
					blk.instructions.set(i, rewrite(instruction));
				}
				blk.condition = (Expr) rewrite(blk.condition);
			}
		}

	}

	private class CopyPropagation {

		private Map<VariableSymbol, Expr> copiedSymbols = new HashMap<>();

		public void compute(MethodDecl md) {
			process(md.cfg.start);
		}

		public void process(BasicBlock blk) {
			Visitor vis = new Visitor();

			// Detect if a phi has arguments which are all the same value.
			for (Phi phi : blk.phis.values()) {
				// bit of a hack, but safe for constants/variables:
				Set<String> strings = new HashSet<>();
				for (Expr rhs : phi.rhs)
					strings.add(AstOneLine.toString(rhs));
				if (strings.size() == 1) {
					Expr rhs = phi.rhs.get(0);
					LOG.debug("Copy Prop: {} is just a copy of {}", phi.lhs,
							rhs);
					copiedSymbols.put(phi.lhs, rhs);
				}
			}

			// Process instructions and condition:
			for (Ast instr : blk.instructions) {
				LOG.debug("Copy Prop: instr={}", instr);
				vis.visit(instr, null);
			}
			if (blk.condition != null)
				blk.condition = (Expr) vis.visit(blk.condition, null);

			// Process any uses in successor phis
			// We COULD process just our own, but it doesn't hurt to
			// process other people's.
			for (BasicBlock succ : blk.successors) {
				for (Phi phi : succ.phis.values()) {
					ListIterator<Expr> rhs = phi.rhs.listIterator();
					while (rhs.hasNext()) {
						Expr replacement = (Expr) vis.visit(rhs.next(), null);
						rhs.set(replacement);
					}
				}
			}

			for (BasicBlock dom : blk.dominatorTreeChildren)
				process(dom);
		}

		class Visitor extends AstRewriteVisitor<Void> {

			@Override
			public Ast assign(Assign ast, Void arg) {
				super.assign(ast, arg);

				if (ast.left() instanceof Var) {
					if (new IsConstantExpr().visit(ast.right(), null)) {
						VariableSymbol sym = ((Var) ast.left()).sym;
						LOG.debug("Copy Prop: {} is just a copy of {}", sym,
								ast.right());
						copiedSymbols.put(sym, ast.right());
					}
				}

				return ast;
			}

			@Override
			public Ast var(Var ast, Void arg) {
				Expr replacement = copiedSymbols.get(ast.sym);
				if (replacement != null) {
					changes++;
					LOG.debug("Copy Prop: Replacing {} with {}", ast,
							replacement);
					return replacement.deepCopy();
				}
				return ast;
			}

			/**
			 * Returns {@code true} if the expression cannot change value during
			 * the course of method execution.
			 */
			class IsConstantExpr extends ExprVisitor<Boolean, Void> {

				@Override
				protected Boolean dfltExpr(Expr ast, Void arg) {
					return false;
				}

				@Override
				public Boolean booleanConst(BooleanConst ast, Void arg) {
					return true;
				}

				@Override
				public Boolean intConst(IntConst ast, Void arg) {
					return true;
				}

				@Override
				public Boolean nullConst(NullConst ast, Void arg) {
					return true;
				}

				@Override
				public Boolean thisRef(ThisRef ast, Void arg) {
					return true;
				}

				@Override
				public Boolean var(Var ast, Void arg) {
					switch (ast.sym.getKind()) {
					case LOCAL:
					case PARAM:
						return true;
					default:
						break;
					}
					return false;
				}

			}

		}
	}

	private class CommonSubexpressionElimination {

		private class Canonical {

			/**
			 * Canonical string representation of the expression. Constructed by
			 * CanonicalizeVisitor.
			 */
			private final String key;

			/**
			 * List of Nodes each of which represents a usage of this
			 * expression.
			 */
			private final List<Expr> appearances;

			/**
			 * Any expression may reference other cse-able expressions. These
			 * two fields, either of which may be null, track those. For
			 * example, the expression "(x0+x1)+(x2+x3)" would have prev1 equal
			 * to the Expression object representing "x0+x1", and prev2 pointing
			 * to the Expression object representing "x2+x3".
			 */
			private final Canonical sub1, sub2;

			/**
			 * An integer count indicating how many of the appearances above
			 * were parts of larger expressions which were cse'd. Consider the
			 * following program fragment:
			 * 
			 * <code>
	              ...
	              y1 = (x0 + x1) + x2
	              y2 = (x0 + x1) + x2
	              ...
	            </code>
			 * 
			 * In this case, both "x0+x1" and "(x0+x1)+x2" will have Expression
			 * nodes, and each will have two appearances. In find(), however, we
			 * will first extract "(x0+x1)+x2" into its own temporary variable.
			 * That will result in code like:
			 * 
			 * <code>
	               ...
	               CSE0 = (x0+x1)+x2
	               ...
	            </code>
			 * 
			 * Now, "x0+x1" will STILL have two appearances recorded in its
			 * list, but one of them is dead; i.e., in a node that has been
			 * removed from the AST.
			 * 
			 * To prevent us from introducing a new CSE variable, we do a walk
			 * down the tree of CSE-able expressions formed by the fields
			 * <code>sub1</code> and <code>sub2</code>, and increment this field
			 * <code>deadAppearances</code>. In the example above, the
			 * <code>deadAppearances</code> field of "x0+x1" would be set to 1,
			 * indicating that one of the appearances listed is no longer in the
			 * AST.
			 */
			private int deadAppearances;

			/**
			 * If we decide to extract this CSE into a temporary variable, it
			 * will be stored here.
			 */
			private VariableSymbol sym;

			Canonical(String key, Canonical sub1, Canonical sub2) {
				this.key = key;
				this.appearances = new ArrayList<>();
				this.sub1 = sub1;
				this.sub2 = sub2;
				this.deadAppearances = 0;
			}

			@Override
			public String toString() {
				return "(" + key + ")";
			}

		}

		private class BlockData {

			private final BasicBlock block;
			private final Map<String, Canonical> canonicalsInScope = new HashMap<>();
			private final List<Canonical> canonicalsAppearingInThisBlock = new ArrayList<>();
			private final List<BlockData> childrenData = new ArrayList<>();

			public BlockData(BasicBlock block) {
				this.block = block;
			}

		}

		/**
		 * First pass: walk down the tree to find and record each expression,
		 * identifying duplicate uses
		 */
		public BlockData find(BlockData domData, BasicBlock block) {
			LOG.debug("CSE.find({}): gathering exprs", block);

			// Initially, any expressions evaluated by our dominators are in
			// scope.
			BlockData data = new BlockData(block);
			if (domData != null)
				data.canonicalsInScope.putAll(domData.canonicalsInScope);

			// Find and record any expressions evaluated in this block.
			ExpressionFinder finder = new ExpressionFinder();
			for (Ast instruction : block.instructions)
				finder.visit(instruction, data);
			if (block.condition != null)
				finder.visit(block.condition, data);

			// Recurse to children.
			if (!block.dominatorTreeChildren.isEmpty()) {
				for (BasicBlock childBlk : block.dominatorTreeChildren)
					data.childrenData.add(find(data, childBlk));
			}

			return data;
		}

		/**
		 * Second pass: walk down the tree again and insert temporaries where
		 * needed to extract CSEs
		 */
		public void replace(MethodSymbol msym, BlockData data) {
			final BasicBlock block = data.block;
			// Process those expressions, extracting duplicates. Note that
			// the list is reversed so that the largest expressions come first.
			LOG.debug("CSE.replace({}): removing exprs", block);
			for (Canonical ex : data.canonicalsAppearingInThisBlock) {
				// If this does not span over at least one subexpression, it's
				// something like "x", "null", or "this", which are not worth
				// it.
				if (ex.sub1 == null && ex.sub2 == null)
					continue;

				// we *could* adjust this criteria; for example we might
				// weight the expression to be substituted by how many
				// evaluations we are saving and opt not to CSE it. There
				// is no sense doing this adjustment without a body of
				// code to profile it on, however. Note the use of
				// deadAppearances to prevent excess CSE-ization. See the
				// comment on the field above for more details.
				if (ex.appearances.size() - ex.deadAppearances <= 1)
					continue;

				// debug print outs.
				LOG.debug("  Expression: {}", ex.key);
				LOG.debug("    Appearances: {}", ex.appearances.size());
				LOG.debug("    Dead Appearances: {}", ex.deadAppearances);

				// Any use of this master expression is also a use of its
				// subexpressions. (i.e., x+y is also a use of x and y)
				// So indicate to those subexpresions that some of their
				// uses have already been replaced.
				addToDeadAppearances(ex.sub1, ex.appearances.size() - 1);
				addToDeadAppearances(ex.sub2, ex.appearances.size() - 1);

				// if this is the first time that this canonical has been
				// replaced,
				// create a symbol.
				Expr expr1;
				if (ex.sym == null) {
					// create a new symbol to use as a temporary
					expr1 = (Expr) ex.appearances.get(0).deepCopy();
					ex.sym = new VariableSymbol("CSE(" + ex.key + ")",
							expr1.type);
					msym.addLocal(ex.sym);
				} else {
					expr1 = null;
				}

				// replace each instance, and insert the assignment that
				// creates the symbol immediately before the first replacement
				for (int i = 0; i < block.instructions.size(); i++) {
					Ast instruction = block.instructions.get(i);
					int changesBefore = changes;
					new ExpressionReplacer().visit(instruction, ex);

					// we need to actually assign a value to the symbol. We wait
					// until right before its first appearance, so that any
					// reference variables are in scope.
					if (expr1 != null && changes > changesBefore) {
						createAssignment(block, i, ex, expr1);
						i++;
						expr1 = null;
					}
				}

				if (block.condition != null) {
					int changesBefore = changes;
					block.condition = (Expr) new ExpressionReplacer().visit(
							block.condition, ex);

					// if we still haven't placed the assignment, the appearance
					// must be in the condition, so just place it at the end
					if (expr1 != null) {
						assert changesBefore < changes;
						createAssignment(block, block.instructions.size(), ex,
								expr1);
						expr1 = null;
					}
				}

				assert expr1 == null;
			}

			for (BlockData chiData : data.childrenData)
				replace(msym, chiData);
		}

		private void createAssignment(BasicBlock block, int i, Canonical ex,
				Expr expr1) {
			LOG.debug("  inserting assignment to {} at index {}", ex.sym, i);
			Assign assign = new Assign(Var.withSym(ex.sym), expr1);
			block.instructions.add(i, assign);
		}

		/**
		 * Indicates that <code>amnt</code> number of the appearances that are
		 * considered dead for each <code>Expression</code> in the list
		 * <code>exprs</code>.
		 */
		private void addToDeadAppearances(Canonical expr, int amnt) {
			if (expr != null) {
				expr.deadAppearances += amnt;
				addToDeadAppearances(expr.sub1, amnt);
				addToDeadAppearances(expr.sub2, amnt);
			}
		}

		/**
		 * This visitor computes canonical string representations of
		 * expressions. For each new expression (that is, an expression not seen
		 * within this block or any that dominate it), a new Expression object
		 * is created and added to the linked list 'exprs'. That linked list
		 * will later be traversed in processBlock() to examine all expressions
		 * that might be cse-able at this block.
		 * 
		 * Note that expressions are added to the list such that the largest
		 * expressions come first; meaning any sub-expressions will be later in
		 * the list. This is important to ensure that we CSE the largest
		 * expressions we can.
		 */
		private class CanonicalizeVisitor extends
				ExprVisitor<Canonical, BlockData> {

			/**
			 * Adds an expr rooted at node <code>n</code> with the hash key
			 * <code>exprkey</code>. The value <code>prev</code> should be the
			 * value of the field <code>exprs</code> before any sub-expressions
			 * were processed; this allows us to add pointers to any Expressions
			 * added since then as they are subexpressions of the current
			 * expressions.
			 */
			public Canonical canonicalize(Expr n, BlockData data,
					String exprkey, Canonical prev1, Canonical prev2) {
				Canonical ex = data.canonicalsInScope.get(exprkey);

				if (ex == null) {
					ex = new Canonical(exprkey, prev1, prev2);
					data.canonicalsInScope.put(ex.key, ex);

					LOG.debug("    New expression: {} (from {})", ex.key, n);
					if (prev1 != null)
						LOG.debug("      Prev1: {}", prev1.key);
					if (prev2 != null)
						LOG.debug("      Prev2: {}", prev2.key);
				} else {
					LOG.debug("    Old expression: {} (from {})", ex.key, n);
				}

				data.canonicalsAppearingInThisBlock.add(ex);
				ex.appearances.add(n);
				return ex;
			}

			/**
			 * Any expression type not explicitly listed cannot be CSE'd, so
			 * return {@code null} by default. However, some sub-expression may
			 * be CSE-able, so we must recursively process them.
			 */
			@Override
			protected Canonical dfltExpr(Expr ast, BlockData data) {
				for (Expr subExpr : ast.childrenOfType(Expr.class))
					visit(subExpr, data);
				return null;
			}

			@Override
			public Canonical binaryOp(BinaryOp ast, BlockData data) {
				Canonical left = visit(ast.left(), data);
				Canonical right = visit(ast.right(), data);
				if (left == null || right == null)
					return null;

				switch (ast.operator) {
				case B_PLUS:
				case B_TIMES:
					// commutative operators
					if (left.key.compareTo(right.key) > 0) {
						Canonical temp = left;
						left = right;
						right = temp;
					}
				default:
					break;
				}

				String c = left + ast.operator.repr + right;
				return canonicalize(ast, data, c, left, right);
			}

			@Override
			public Canonical booleanConst(BooleanConst ast, BlockData data) {
				return canonicalize(ast, data, "" + ast.value, null, null);
			}

			@Override
			public Canonical cast(Cast ast, BlockData data) {
				Canonical arg = visit(ast.arg(), data);
				if (arg == null)
					return null;

				String c = format("({}){}", ast.type.name, arg);
				return canonicalize(ast, data, c, arg, null);
			}

			@Override
			public Canonical intConst(IntConst ast, BlockData data) {
				return canonicalize(ast, data, "" + ast.value, null, null);
			}

			@Override
			public Canonical nullConst(NullConst ast, BlockData data) {
				return canonicalize(ast, data, "null", null, null);
			}

			@Override
			public Canonical thisRef(ThisRef ast, BlockData data) {
				return canonicalize(ast, data, "this", null, null);
			}

			@Override
			public Canonical unaryOp(UnaryOp ast, BlockData data) {
				Canonical arg = visit(ast.arg(), data);
				if (arg == null)
					return null;

				String c = ast.operator.repr + arg;
				return canonicalize(ast, data, c, arg, null);
			}

			@Override
			public Canonical var(Var ast, BlockData data) {
				VariableSymbol varSym = ast.sym;
				switch (varSym.getKind()) {
				case FIELD: // removed by SemanticAnalyzer
					break;
				case LOCAL:
				case PARAM:
					// Since we in SSA form, the name of the variable is
					// sufficient to distinguish it completely. We don't
					// handle any symbols that are not versioned. Note
					// that we don't add an Expression, as it's not worth
					// CSEing a variable reference.
					return canonicalize(ast, data, varSym.name, null, null);
				}
				return null;
			}

		}

		private class ExpressionFinder extends AstVisitor<Void, BlockData> {

			@Override
			public Void visit(Ast ast, BlockData arg) {
				LOG.debug("  search {}", ast);
				return super.visit(ast, arg);
			}

			@Override
			protected Void dfltExpr(Expr ast, BlockData data) {
				new CanonicalizeVisitor().visit(ast, data);
				return null;
			}

			@Override
			public Void assign(Assign ast, BlockData arg) {
				if (ast.left() instanceof Var) {
					// careful about assignments to variables, they are not
					// uses of that variable
					return visit(ast.right(), arg);
				} else {
					return super.assign(ast, arg);
				}
			}

		}

		private class ExpressionReplacer extends AstRewriteVisitor<Canonical> {

			@Override
			protected Ast dfltExpr(Expr ast, Canonical arg) {
				if (arg.appearances.contains(ast)) {
					changes++;
					return Var.withSym(arg.sym);
				}
				return super.dfltExpr(ast, arg);
			}

			@Override
			public Ast assign(Assign ast, Canonical arg) {
				if (ast.left() instanceof Var) {
					// careful about assignments to variables, they are not
					// uses of that variable
					ast.setRight((Expr) visit(ast.right(), arg));
					return ast;
				} else {
					return super.assign(ast, arg);
				}
			}

		}

		public void compute(MethodDecl md) {
			BlockData data = find(null, md.cfg.start);
			replace(md.sym, data);
		}

	}

}
