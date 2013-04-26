package cd.ir.ast;

import java.util.Collections;
import java.util.List;

import cd.ir.AstVisitor;
import cd.ir.ControlFlowGraph;
import cd.ir.symbols.MethodSymbol;
import cd.util.Pair;

public class MethodDecl extends Decl {

	public final String returnType;
	public final String name;
	public final List<String> argumentTypes;
	public final List<String> argumentNames;

	public MethodSymbol sym;
	public ControlFlowGraph cfg;

	public MethodDecl(String returnType, String name,
			List<Pair<String>> formalParams, Seq decls, Seq body) {
		this(returnType, name, Pair.unzipA(formalParams), Pair
				.unzipB(formalParams), decls, body);
	}

	public MethodDecl(String returnType, String name,
			List<String> argumentTypes, List<String> argumentNames, Seq decls,
			Seq body) {
		super(2);
		this.returnType = returnType;
		this.name = name;
		this.argumentTypes = argumentTypes;
		this.argumentNames = argumentNames;
		setDecls(decls);
		setBody(body);
	}

	public Seq decls() {
		return (Seq) this.rwChildren.get(0);
	}

	public void setDecls(Seq decls) {
		this.rwChildren.set(0, decls);
	}

	public Seq body() {
		return (Seq) this.rwChildren.get(1);
	}

	public void setBody(Seq body) {
		this.rwChildren.set(1, body);
	}

	@Override
	public <R, A> R accept(AstVisitor<R, A> visitor, A arg) {
		return visitor.methodDecl(this, arg);
	}

	@Override
	public Ast deepCopy() {
		return new MethodDecl(returnType, name,
				Collections.unmodifiableList(argumentTypes),
				Collections.unmodifiableList(argumentNames), (Seq) decls()
						.deepCopy(), (Seq) body().deepCopy());
	}

}