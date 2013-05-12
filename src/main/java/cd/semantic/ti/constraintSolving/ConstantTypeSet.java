package cd.semantic.ti.constraintSolving;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import cd.ir.symbols.TypeSymbol;

import com.google.common.collect.ImmutableSet;

public class ConstantTypeSet implements TypeSet {
	private final ImmutableSet<TypeSymbol> types;

	public ConstantTypeSet(TypeSymbol... syms) {
		this.types = ImmutableSet.copyOf(syms);
	}

	public ConstantTypeSet(Set<? extends TypeSymbol> types) {
		this.types = ImmutableSet.copyOf(types);
	}

	@Override
	public ImmutableSet<TypeSymbol> getTypes() {
		return types;
	}

	public ConstantTypeSet intersect(ConstantTypeSet typeSet) {
		Set<TypeSymbol> typeSyms = new HashSet<>(types);
		typeSyms.retainAll(typeSet.getTypes());
		return new ConstantTypeSet(typeSyms);
	}

	@Override
	public boolean isSubsetOf(TypeSet other) {
		return other.getTypes().containsAll(types);
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