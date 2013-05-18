package cd.semantic.ti.constraintSolving;

import java.util.HashSet;
import java.util.Set;

import cd.ir.symbols.TypeSymbol;
import cd.util.NonnullByDefault;

import com.google.common.collect.ImmutableSet;

/**
 * Represents a type variable that holds sets of type symbols that may be
 * manipulated when solving.
 */
@NonnullByDefault
public class TypeVariable extends TypeSet {
	
	private final Set<TypeSymbol> types;
	private final String description;

	public TypeVariable(String description) {
		this.types = new HashSet<>();
		this.description = description;
	}

	// Third-party library is missing non-null annotations
	@SuppressWarnings("null")
	@Override
	public Set<TypeSymbol> getTypes() {
		return ImmutableSet.copyOf(types);
	}

	@Override
	public boolean isSubsetOf(TypeSet other) {
		return other.getTypes().containsAll(types);
	}

	/**
	 * Make this type variable include all the types in the other type set.
	 */
	public void extend(TypeSet other) {
		types.addAll(other.getTypes());
	}

	@Override
	public String toString() {
		return getDescription();
	}

	public String getDescription() {
		return description;
	}

	@Override
	public <R, A> R accept(TypeSetVisitor<R, A> visitor, A arg) {
		return visitor.visit(this, arg);
	}

}