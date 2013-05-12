package cd.semantic.ti;

import cd.ir.symbols.ClassSymbol;
import cd.ir.symbols.VariableSymbol;
import cd.semantic.TypeSymbolTable;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

/**
 * Cache that makes it possible to efficiently look up all ClassSymbols that have a field with a certain name.
 */
public final class ClassSymbolFieldCache {

	private final ImmutableMultimap<String, ClassSymbol>  map;

	private ClassSymbolFieldCache(ImmutableMultimap<String, ClassSymbol> map) {
		this.map = map;
	}

	/**
	 * Build the cache from all methods in the given type symbol table.
	 */
	public static ClassSymbolFieldCache of(TypeSymbolTable typeSymbols) {
		Multimap<String, ClassSymbol> map = LinkedHashMultimap.create();
		for (ClassSymbol classSymbol : typeSymbols.getClassSymbols()) {
			for (VariableSymbol fieldVarSym : classSymbol.getDeclaredFields()) {
				String name = fieldVarSym.name;
				map.put(name, classSymbol);
			}
		}
		return new ClassSymbolFieldCache(ImmutableMultimap.copyOf(map));
	}

	/**
	 * Returns a collection of all class symbols in the cache that have a
	 * certain field
	 * 
	 * @param name
	 *            the field name that the class symbols must share
	 * @return the newly-constructed cache
	 */
	public ImmutableCollection<ClassSymbol> get(String name) {
		return map.get(name);
	}
}
