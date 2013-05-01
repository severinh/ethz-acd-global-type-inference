package cd.semantic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cd.exceptions.SemanticFailure;
import cd.exceptions.SemanticFailure.Cause;
import cd.ir.symbols.Symbol;

/**
 * A simple symbol table, with a pointer to the enclosing scope. Used to store
 * the various scopes for local variable, parameter, and field lookup.
 */
public class SymbolTable<S extends Symbol> {

	private final Map<String, S> map;
	private final SymbolTable<S> parent;

	public SymbolTable(SymbolTable<S> parent) {
		this.map = new HashMap<>();
		this.parent = parent;
	}

	public void add(S sym) {
		// Check that the symbol is not already declared *at this level*
		if (containsLocally(sym.name)) {
			throw new SemanticFailure(Cause.DOUBLE_DECLARATION,
					"Symbol '%s' was declared twice in the same scope",
					sym.name);
		}
		map.put(sym.name, sym);
	}

	public List<S> allSymbols() {
		List<S> result = new ArrayList<>();
		SymbolTable<S> currentTable = this;
		while (currentTable != null) {
			result.addAll(currentTable.map.values());
			currentTable = currentTable.parent;
		}
		return result;
	}

	public Collection<S> localSymbols() {
		return Collections.unmodifiableCollection(map.values());
	}

	/**
	 * True if there is a declaration with the given name at any level in the
	 * symbol table
	 */
	public boolean contains(String name) {
		return get(name) != null;
	}

	/**
	 * True if there is a declaration at THIS level in the symbol table; may
	 * return {@code false} even if a declaration exists in some enclosing
	 * scope.
	 */
	public boolean containsLocally(String name) {
		return map.containsKey(name);
	}

	/**
	 * Base method: returns {@code null} if no symbol by that name can be found,
	 * in this table or in its parents.
	 */
	public S get(String name) {
		S res = map.get(name);
		if (res != null) {
			return res;
		} else if (parent == null) {
			return null;
		}
		return parent.get(name);
	}

}