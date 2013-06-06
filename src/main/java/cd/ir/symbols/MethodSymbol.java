package cd.ir.symbols;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.base.Optional;

import cd.exceptions.SemanticFailure;
import cd.semantic.SymbolTable;

public class MethodSymbol extends Symbol {

	private final SymbolTable<VariableSymbol> scope;
	private final Map<String, VariableSymbol> locals;
	private final List<VariableSymbol> parameters;

	private final ClassSymbol owner;
	private final Optional<MethodSymbol> overrides;
	private final List<MethodSymbol> overriddenBy;
	public TypeSymbol returnType;
	public int vtableIndex = -1;
	public boolean used = false;

	// Third-party library is missing non-null annotations
	@SuppressWarnings("null")
	public MethodSymbol(String name, ClassSymbol owner) {
		super(name);
		
		this.scope = new SymbolTable<>(owner.getScope());
		this.scope.add(owner.getThisSymbol());
		this.locals = new LinkedHashMap<>();
		this.parameters = new ArrayList<>();
		this.owner = owner;
		this.returnType = BottomTypeSymbol.INSTANCE;
		this.overriddenBy = new ArrayList<>();

		Optional<MethodSymbol> overrides = Optional.absent();
		if (owner.getSuperClass().isPresent()) {
			ClassSymbol superClass = owner.getSuperClass().get();
			MethodSymbol nullOverrides = superClass.tryGetMethod(name);
			if (nullOverrides != null) {
				overrides = Optional.of(nullOverrides);
				nullOverrides.addOverrider(this);
			}
		}
		this.overrides = overrides;
	}

	private void addOverrider(MethodSymbol methodSymbol) {
		overriddenBy.add(methodSymbol);
		if (overrides.isPresent()) {
			MethodSymbol parent = overrides.get();
			parent.addOverrider(methodSymbol);
		}
	}

	@Override
	public String toString() {
		return name + "(...)";
	}

	public SymbolTable<VariableSymbol> getScope() {
		return scope;
	}

	// Third-party library is missing non-null annotations
	@SuppressWarnings("null")
	public Collection<VariableSymbol> getLocals() {
		return Collections.unmodifiableCollection(locals.values());
	}

	public Collection<VariableSymbol> getLocalsAndParameters() {
		List<VariableSymbol> result = new ArrayList<>();
		result.addAll(getLocals());
		result.addAll(getParameters());
		return result;
	}

	@Nullable
	public VariableSymbol getLocal(String name) {
		return locals.get(name);
	}

	// Third-party library is missing non-null annotations
	@SuppressWarnings("null")
	public List<VariableSymbol> getParameters() {
		return Collections.unmodifiableList(parameters);
	}

	public VariableSymbol getParameter(int index) {
		VariableSymbol result = parameters.get(index);
		return result;
	}

	@Nullable
	public VariableSymbol getParameter(String name) {
		for (VariableSymbol parameter : parameters) {
			if (parameter.name.equals(name)) {
				return parameter;
			}
		}
		return null;
	}

	public List<MethodSymbol> getOverriddenBy() {
		return overriddenBy;
	}

	public boolean isOverridden() {
		return !overriddenBy.isEmpty();
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

	/**
	 * Returns the class that declares this method.
	 */
	public ClassSymbol getOwner() {
		return owner;
	}

	/**
	 * Returns the method that this method overrides, if any.
	 */
	public Optional<MethodSymbol> getOverriddenMethod() {
		return overrides;
	}

}