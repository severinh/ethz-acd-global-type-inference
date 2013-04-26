package cd.ir.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cd.ir.AstVisitor;
import cd.ir.symbols.ClassSymbol;

public class ClassDecl extends Decl {

	public final String name;
	public final String superClass;
	public ClassSymbol sym;

	public ClassDecl(String name, String superClass, List<? extends Ast> members) {
		super(-1);
		this.name = name;
		this.superClass = superClass;
		this.rwChildren.addAll(members);
	}

	public List<Ast> members() {
		return Collections.unmodifiableList(this.rwChildren);
	}

	public List<VarDecl> fields() {
		return childrenOfType(VarDecl.class);
	} // includes constants!

	public List<MethodDecl> methods() {
		return childrenOfType(MethodDecl.class);
	}

	@Override
	public <R, A> R accept(AstVisitor<R, A> visitor, A arg) {
		return visitor.classDecl(this, arg);
	}

	@Override
	public Ast deepCopy() {
		List<Ast> result = new ArrayList<>();

		for (final Ast ast : members()) {
			result.add(ast.deepCopy());
		}

		return new ClassDecl(name, superClass, result);
	}

}