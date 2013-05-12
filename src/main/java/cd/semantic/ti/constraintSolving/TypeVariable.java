package cd.semantic.ti.constraintSolving;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import cd.ir.symbols.TypeSymbol;

import com.google.common.collect.ImmutableSet;

/**
 * Represents a type variable that holds sets of type symbols that may be
 * manipulated when solving.
 */
public class TypeVariable implements TypeSet {
	private final Set<TypeSymbol> types;

	public TypeVariable() {
		this.types = new HashSet<>();
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
	 * Make this type variable include all the types in the other type set.
	 */
	public void extend(TypeSet other) {
		types.addAll(other.getTypes());
	}

	@Override
	public String toString() {
		if (types.isEmpty()) {
			return "\u2205";
		} else {
			return "{" + StringUtils.join(types, ",") + "}";
		}
	}
}