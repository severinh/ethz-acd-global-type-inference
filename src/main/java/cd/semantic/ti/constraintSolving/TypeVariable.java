package cd.semantic.ti.constraintSolving;

import java.util.HashSet;
import java.util.Set;

import cd.ir.symbols.TypeSymbol;

import com.google.common.collect.ImmutableSet;

/**
 * Represents a type variable that holds sets of typesymbols that may be
 * manipulated when solving
 * 
 */
public class TypeVariable implements TypeSet {
	private Set<TypeSymbol> types = new HashSet<>();

	public TypeVariable() {

	}

	@Override
	public Set<TypeSymbol> getTypes() {
		return ImmutableSet.copyOf(types);
	}

	@Override
	public boolean isSubsetOf(TypeSet other) {
		return other.getTypes().containsAll(types);
	}
	
	/**
	 * Make this type variable include all the types in the other typeset
	 * @param other
	 */
	public void extend(TypeSet other) {
		types.addAll(other.getTypes());
	}
}