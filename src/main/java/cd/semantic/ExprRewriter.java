package cd.semantic;

import cd.ir.ast.Ast;
import cd.ir.ast.AstRewriteVisitor;
import cd.ir.ast.Field;
import cd.ir.ast.ThisRef;
import cd.ir.ast.Var;

/**
 * Runs after the semantic check and rewrites expressions to be more normalized.
 * For example, references to a field {@code foo} are rewritten to always use
 * {@link Field} objects (i.e., {@code this.foo}.
 */
public class ExprRewriter extends AstRewriteVisitor<Void> {

	@Override
	public Ast var(Var ast, Void arg) {
		switch (ast.getSymbol().getKind()) {
		case PARAM:
		case LOCAL:
			// Leave params or local variables alone
			return ast;
		case FIELD:
			// Convert an implicit field reference to "this.foo"
			Field f = new Field(new ThisRef(), ast.getName());
			f.sym = ast.getSymbol();
			f.type = ast.type;
			return f;
		}
		throw new RuntimeException("Unknown kind of var");
	}

}