package cd.ir.symbols;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import cd.exceptions.SemanticFailure;
import cd.semantic.SymbolTable;

public class MethodSymbol extends Symbol {

	// TODO: TypedSemanticAnalyzer internally builds a symbol table for each
	// method on its own. Because such a variable symbol table is also needed
	// earlier in type inference, build it openly and early in MethodSymbol.
	// TypedSemanticAnalyzer should eventually reuse this symbol table.
	private final SymbolTable<VariableSymbol> scope;

	private final Map<String, VariableSymbol> locals;
	private final List<VariableSymbol> parameters;

	public final ClassSymbol owner;

	public TypeSymbol returnType;
	public int vtableIndex = -1;
	public MethodSymbol overrides;

	public MethodSymbol(String name, ClassSymbol owner) {
		super(name);
		this.scope = new SymbolTable<>();
		this.scope.add(owner.thisSymbol);
		this.locals = new LinkedHashMap<>();
		this.parameters = new ArrayList<>();
		this.owner = owner;
	}

	@Override
	public String toString() {
		return name + "(...)";
	}

	public SymbolTable<VariableSymbol> getScope() {
		return scope;
	}

	public Collection<VariableSymbol> getLocals() {
		return Collections.unmodifiableCollection(locals.values());
	}

	public VariableSymbol getLocal(String name) {
		return locals.get(name);
	}

	public List<VariableSymbol> getParameters() {
		return Collections.unmodifiableList(parameters);
	}

	public VariableSymbol getParameter(int index) {
		return parameters.get(index);
	}

	public VariableSymbol getParameter(String name) {
		for (VariableSymbol parameter : parameters) {
			if (parameter.name.equals(name)) {
				return parameter;
			}
		}
		return null;
	}

	/**
	 * Registers a new local variable as being declared in this method.
	 * 
	 * @param local
	 *            the new local variable symbol
	 * @throws SemanticFailure
	 *             if there is already a local variable with the same name in
	 *             this method
	 */
	public void addLocal(VariableSymbol local) {
		scope.add(local);
		locals.put(local.name, local);
	}

	/**
	 * Registers a new formal parameter for this method.
	 * 
	 * @param parameter
	 *            the new parameter variable symbol
	 * @throws SemanticFailure
	 *             if there is already a parameter variable with the same name
	 *             in this method
	 */
	public void addParameter(VariableSymbol parameter) {
		scope.add(parameter);
		parameters.add(parameter);
	}

}