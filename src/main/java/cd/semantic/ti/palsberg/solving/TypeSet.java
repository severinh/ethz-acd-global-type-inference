package cd.semantic.ti.palsberg.solving;

import java.util.Set;

import org.apache.commons.lang.StringUtils;

import cd.ir.symbols.TypeSymbol;
import cd.util.NonnullByDefault;

@NonnullByDefault
public abstract class TypeSet {

	public abstract Set<TypeSymbol> getTypes();

	public abstract boolean isSubsetOf(TypeSet other);

	public abstract <R, A> R accept(TypeSetVisitor<R, A> visitor, A arg);

	/**
	 * Returns a string showing the actual types contained in the set.
	 * 
	 * @return the empty set symbol if the type set is empty
	 */
	public String getTypeSetString() {
		Set<TypeSymbol> types = getTypes();
		if (types.isEmpty()) {
			return "\u2205";
		} else {
			return "{" + StringUtils.join(types, ",") + "}";
		}
	}

}