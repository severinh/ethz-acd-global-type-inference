package cd.ir.symbols;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import cd.exceptions.SemanticFailure;
import cd.exceptions.SemanticFailure.Cause;

public class MethodSymbol extends Symbol {

	private final Map<String, VariableSymbol> locals = new LinkedHashMap<>();
	private final List<VariableSymbol> parameters = new ArrayList<>();

	public final ClassSymbol owner;

	public TypeSymbol returnType;
	public int vtableIndex = -1;
	public MethodSymbol overrides;

	public MethodSymbol(String name, ClassSymbol owner) {
		super(name);
		this.owner = owner;
	}

	@Override
	public String toString() {
		return name + "(...)";
	}

	public Collection<VariableSymbol> getLocals() {
		return Collections.unmodifiableCollection(locals.values());
	}

	public List<VariableSymbol> getParameters() {
		return Collections.unmodifiableList(parameters);
	}

	public VariableSymbol getParameter(int index) {
		return parameters.get(index);
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
		if (locals.containsKey(local.name)) {
			throw new SemanticFailure(
					Cause.DOUBLE_DECLARATION,
					"Local '%s' was declared twice in the same method '%s' of class '%s'",
					local.name, name, owner.name);
		}

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
		// The quadratic complexity of the following check should not be an
		// issue as methods usually only have few parameters. Furthermore,
		// adding parameters happens less often than accessing parameters.
		for (VariableSymbol existingParameter : parameters) {
			if (existingParameter.name.equals(parameter.name)) {
				throw new SemanticFailure(
						Cause.DOUBLE_DECLARATION,
						"Paremeter '%s' was declared twice in the same method '%s' of class '%s'",
						parameter.name, name, owner.name);
			}
		}

		parameters.add(parameter);
	}

}