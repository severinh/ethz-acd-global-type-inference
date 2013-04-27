package cd.ir.symbols;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MethodSymbol extends Symbol {

	public final Map<String, VariableSymbol> locals = new HashMap<>();
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

}