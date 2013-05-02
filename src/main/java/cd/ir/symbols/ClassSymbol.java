package cd.ir.symbols;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import cd.exceptions.SemanticFailure;
import cd.exceptions.SemanticFailure.Cause;
import cd.semantic.SymbolTable;

public class ClassSymbol extends TypeSymbol {

	private final ClassSymbol superClass;
	private final VariableSymbol thisSymbol;

	private final SymbolTable<VariableSymbol> scope;
	private final Map<String, VariableSymbol> fields;
	private final Map<String, MethodSymbol> methods;

	public int totalMethods = -1;
	public int totalFields = -1;
	public int sizeof = -1;

	public ClassSymbol(String name, ClassSymbol superClass) {
		super(name);

		SymbolTable<VariableSymbol> superScope = null;
		if (superClass != null) {
			superScope = superClass.getScope();
		}

		this.superClass = superClass;
		this.thisSymbol = new VariableSymbol("this", this);
		this.scope = new SymbolTable<>(superScope);
		this.fields = new LinkedHashMap<>();
		this.methods = new LinkedHashMap<>();
	}

	public ClassSymbol(String name) {
		this(name, null);
	}

	public ClassSymbol getSuperClass() {
		return superClass;
	}

	@Override
	public boolean isReferenceType() {
		return true;
	}

	public SymbolTable<VariableSymbol> getScope() {
		return scope;
	}

	public VariableSymbol getThisSymbol() {
		return thisSymbol;
	}

	public VariableSymbol getField(String name) {
		VariableSymbol fieldSymbol = fields.get(name);
		if (fieldSymbol == null && superClass != null) {
			return superClass.getField(name);
		}
		return fieldSymbol;
	}

	public MethodSymbol getMethod(String name) {
		MethodSymbol methodSymbol = methods.get(name);
		if (methodSymbol == null && superClass != null) {
			return superClass.getMethod(name);
		}
		return methodSymbol;
	}

	/**
	 * Returns the list of fields declared in this class.
	 * 
	 * @return the list of fields, not including the ones of super-classes
	 */
	public Collection<VariableSymbol> getDeclaredFields() {
		return Collections.unmodifiableCollection(fields.values());
	}

	/**
	 * Returns the list of methods declared in this class.
	 * 
	 * @return the list of methods, not including the ones of super-classes
	 */
	public Collection<MethodSymbol> getDeclaredMethods() {
		return Collections.unmodifiableCollection(methods.values());
	}

	/**
	 * Registers a new method as being declared in this class.
	 * 
	 * @param method
	 *            the new method
	 * @throws SemanticFailure
	 *             if there is already a method with the same name in this class
	 */
	public void addMethod(MethodSymbol method) {
		if (methods.containsKey(method.name)) {
			throw new SemanticFailure(Cause.DOUBLE_DECLARATION,
					"Method '%s' was declared twice in the same class '%s'",
					method.name, name);
		}

		methods.put(method.name, method);
	}

	/**
	 * Registers a new field as being declared in this class.
	 * 
	 * @param field
	 *            the new field
	 * @throws SemanticFailure
	 *             if there is already a field with the same name in this class
	 */
	public void addField(VariableSymbol field) {
		scope.add(field);
		fields.put(field.name, field);
	}

}