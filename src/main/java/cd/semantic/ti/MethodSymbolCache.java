package cd.semantic.ti;

import java.util.Objects;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

import cd.ir.symbols.ClassSymbol;
import cd.ir.symbols.MethodSymbol;
import cd.semantic.TypeSymbolTable;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkArgument;

/**
 * Cache that allows an efficient lookup of all non-overriding method symbols
 * with a certain name and a certain number of parameters.
 * <p>
 * Note that the cache does <em>not</em> provide overriding method symbols. In
 * Javali, the parameter and return types are invariant. This allows us to treat
 * the method symbol closest to the top of the type hierarchy as the canonical
 * method symbol and ignore the ones overriding it in subclasses. For them, one
 * can simply copy the parameter and return types.
 */
public final class MethodSymbolCache {

	private final ImmutableMultimap<Key, MethodSymbol> map;

	private MethodSymbolCache(ImmutableMultimap<Key, MethodSymbol> map) {
		this.map = map;
	}

	/**
	 * Build the cache from all methods in the given type symbol table.
	 */
	public static MethodSymbolCache of(TypeSymbolTable typeSymbols) {
		Multimap<Key, MethodSymbol> map = LinkedHashMultimap.create();
		for (ClassSymbol classSymbol : typeSymbols.getClassSymbols()) {
			for (MethodSymbol methodSymbol : classSymbol.getDeclaredMethods()) {
				// Ignore overriding methods
				if (methodSymbol.overrides == null) {
					String name = methodSymbol.name;
					int parameterCount = methodSymbol.getParameters().size();
					Key key = new Key(name, parameterCount);
					map.put(key, methodSymbol);
				}
			}
		}
		return new MethodSymbolCache(ImmutableMultimap.copyOf(map));
	}

	/**
	 * Returns a collection of all method symbols in the cache that have a
	 * certain name and a certain number of parameters.
	 * 
	 * @param name
	 *            the name that the method symbols must share
	 * @param parameterCount
	 *            the number of parameters that the method symbols must have
	 * @return the requested collection of method symbols
	 */
	public ImmutableCollection<MethodSymbol> get(String name, int parameterCount) {
		Key key = new Key(name, parameterCount);
		return map.get(key);
	}

	private static class Key {

		private final String name;
		private final int parameterCount;

		public Key(String name, int parameterCount) {
			super();

			checkArgument(parameterCount >= 0);
			this.name = checkNotNull(name);
			this.parameterCount = parameterCount;
		}

		@Override
		public int hashCode() {
			return Objects.hash(name, parameterCount);
		}

		@Override
		public boolean equals(@Nullable Object obj) {
			if (obj instanceof Key) {
				Key other = (Key) obj;
				return Objects.equals(name, other.name)
						&& parameterCount == other.parameterCount;
			} else {
				return false;
			}
		}

		@Override
		public String toString() {
			return "Key [name=" + name + ", parameterCount=" + parameterCount
					+ "]";
		}

	}

}
