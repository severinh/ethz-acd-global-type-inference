package cd.semantic.ti.constraintSolving;

import java.util.HashSet;
import java.util.Set;

import cd.ir.symbols.TypeSymbol;

import com.google.common.collect.ImmutableSet;

public class ConstantTypeSet implements TypeSet {
	private final ImmutableSet<TypeSymbol> types;

	public ConstantTypeSet(TypeSymbol... syms) {
		this.types = ImmutableSet.copyOf(syms);
	}

	public ConstantTypeSet(Set<TypeSymbol> types) {
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
}