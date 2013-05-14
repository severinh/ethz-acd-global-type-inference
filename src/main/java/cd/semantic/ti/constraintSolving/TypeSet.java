package cd.semantic.ti.constraintSolving;

import java.util.Set;

import cd.ir.symbols.TypeSymbol;

public interface TypeSet {

	public Set<TypeSymbol> getTypes();

	public boolean isSubsetOf(TypeSet other);

	public <R, A> R accept(TypeSetVisitor<R, A> visitor, A arg);

}