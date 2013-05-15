package cd.semantic.ti.constraintSolving;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import cd.ir.symbols.TypeSymbol;
import cd.util.NonnullByDefault;

import com.google.common.collect.ImmutableSet;

@NonnullByDefault
public class ConstantTypeSet implements TypeSet {

	private final ImmutableSet<TypeSymbol> types;

	// Third-party library is missing non-null annotations
	@SuppressWarnings("null")
	public ConstantTypeSet(TypeSymbol... syms) {
		this.types = ImmutableSet.copyOf(syms);
	}

	// Third-party library is missing non-null annotations
	@SuppressWarnings("null")
	public ConstantTypeSet(Set<? extends TypeSymbol> types) {
		this.types = ImmutableSet.copyOf(types);
	}

	public ConstantTypeSet(ImmutableSet<TypeSymbol> types) {
		this.types = types;
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

	@Override
	public <R, A> R accept(TypeSetVisitor<R, A> visitor, A arg) {
		return visitor.visit(this, arg);
	}

}