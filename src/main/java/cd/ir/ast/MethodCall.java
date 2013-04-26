package cd.ir.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cd.ir.AstVisitor;
import cd.ir.symbols.MethodSymbol;

public class MethodCall extends Stmt {

	public final String methodName;

	public MethodSymbol sym;

	public MethodCall(Expr rcvr, String methodName, List<Expr> arguments) {
		super(-1);
		assert rcvr != null && methodName != null && arguments != null;
		this.methodName = methodName;
		this.rwChildren.add(rcvr);
		this.rwChildren.addAll(arguments);
	}

	public MethodCall(MethodCallExpr mce) {
		super(-1);
		this.methodName = mce.methodName;
		this.rwChildren.addAll(mce.rwChildren);
	}

	/**
	 * Returns the receiver of the method call. i.e., for a method call
	 * {@code a.b(c,d)} returns {@code a}.
	 */
	public Expr receiver() {
		return (Expr) this.rwChildren.get(0);
	}

	/**
	 * Changes the receiver of the method call. i.e., for a method call
	 * {@code a.b(c,d)} changes {@code a}.
	 */
	public void setReceiver(Expr rcvr) {
		this.rwChildren.set(0, rcvr);
	}

	/**
	 * Returns all arguments to the method, <b>including the receiver.</b> i.e,
	 * for a method call {@code a.b(c,d)} returns {@code [a, c, d]}
	 */
	public List<Expr> allArguments() {
		ArrayList<Expr> result = new ArrayList<>();
		for (Ast chi : this.rwChildren)
			result.add((Expr) chi);
		return Collections.unmodifiableList(result);
	}

	/**
	 * Returns all arguments to the method, without the receiver. i.e, for a
	 * method call {@code a.b(c,d)} returns {@code [c, d]}
	 */
	public List<Expr> argumentsWithoutReceiver() {
		ArrayList<Expr> result = new ArrayList<>();
		for (int i = 1; i < this.rwChildren.size(); i++)
			result.add((Expr) this.rwChildren.get(i));
		return Collections.unmodifiableList(result);
	}

	@Override
	public <R, A> R accept(AstVisitor<R, A> visitor, A arg) {
		return visitor.methodCall(this, arg);
	}

	public List<Expr> deepCopyArguments() {
		ArrayList<Expr> result = new ArrayList<>();

		for (final Expr expr : argumentsWithoutReceiver()) {
			result.add((Expr) expr.deepCopy());
		}

		return result;

	}

	@Override
	public Ast deepCopy() {
		return new MethodCall((Expr) receiver().deepCopy(), methodName,
				deepCopyArguments());
	}

}