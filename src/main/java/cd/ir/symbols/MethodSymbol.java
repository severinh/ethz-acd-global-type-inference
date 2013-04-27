package cd.ir.symbols;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cd.exceptions.SemanticFailure;
import cd.exceptions.SemanticFailure.Cause;

public class MethodSymbol extends Symbol {

	private final Map<String, VariableSymbol> locals = new HashMap<>();

	public final List<VariableSymbol> parameters = new ArrayList<>();
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

	public void addLocal(VariableSymbol local) {
		if (locals.containsKey(local.name)) {
			throw new SemanticFailure(
					Cause.DOUBLE_DECLARATION,
					"Local '%s' was declared twice in the same method '%s' of class '%s'",
					local.name, name, owner.name);
		}

		locals.put(local.name, local);
	}

}