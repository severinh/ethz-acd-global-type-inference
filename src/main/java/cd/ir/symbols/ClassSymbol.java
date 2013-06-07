package cd.ir.symbols;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.Nullable;

import cd.exceptions.SemanticFailure;
import cd.exceptions.SemanticFailure.Cause;
import cd.semantic.SymbolTable;

import com.google.common.base.Optional;

public class ClassSymbol extends TypeSymbol {

	private final Optional<ClassSymbol> superClass;
	private final VariableSymbol thisSymbol;

	private final SymbolTable<VariableSymbol> scope;
	private final Map<String, VariableSymbol> fields;
	private final Map<String, MethodSymbol> methods;

	public int totalMethods = -1;
	public int totalFields = -1;
	public int sizeof = -1;

	private ClassSymbol(String name, Optional<ClassSymbol> superClass) {
		super(name);

		SymbolTable<VariableSymbol> superScope = null;
		if (superClass.isPresent()) {
			superScope = superClass.get().getScope();
		}

		this.superClass = superClass;
		this.thisSymbol = new VariableSymbol("this", this);
		this.scope = new SymbolTable<>(superScope);
		this.fields = new LinkedHashMap<>();
		this.methods = new LinkedHashMap<>();
	}

	public ClassSymbol(String name, ClassSymbol superClass) {
		this(name, Optional.of(superClass));
	}

	public static ClassSymbol makeObjectClass() {
		ClassSymbol result = new ClassSymbol("Object",
				Optional.<ClassSymbol> absent());
		return result;
	}

	public Optional<ClassSymbol> getSuperClass() {
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

	/**
	 * Returns the variable symbol for the field with the given name in this
	 * class or any superclass.
	 * 
	 * @param name
	 *            the name of the field
	 * @return the variable symbol representing the field
	 * @throws SemanticFailure
	 *             if there is no field of that name in any superclass
	 */
	public VariableSymbol getField(String name) {
		VariableSymbol fieldSymbol = tryGetField(name);
		if (fieldSymbol == null) {
			throw new SemanticFailure(Cause.NO_SUCH_FIELD,
					"Type %s has no field %s", this, name);
		}
		return fieldSymbol;
	}

	@Nullable
	VariableSymbol tryGetField(String name) {
		VariableSymbol fieldSymbol = fields.get(name);
		if (fieldSymbol == null && superClass.isPresent()) {
			return superClass.get().tryGetField(name);
		}
		return fieldSymbol;
	}

	/**
	 * Returns the method symbol for the method with the given name in this
	 * class or any superclass.
	 * 
	 * @param name
	 *            the name of the method
	 * @return the method symbol
	 * @throws SemanticFailure
	 *             if there is no method of that name in any superclass
	 */
	public MethodSymbol getMethod(String name) {
		MethodSymbol methodSymbol = tryGetMethod(name);
		if (methodSymbol == null) {
			throw new SemanticFailure(Cause.NO_SUCH_FIELD,
					"Type %s has no method %s", this, name);
		}
		return methodSymbol;
	}

	@Nullable
	MethodSymbol tryGetMethod(String name) {
		MethodSymbol methodSymbol = methods.get(name);
		if (methodSymbol == null && superClass.isPresent()) {
			return superClass.get().tryGetMethod(name);
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
		checkArgument(method.getOwner().equals(this));

		if (methods.containsKey(method.name)) {
			throw new SemanticFailure(Cause.DOUBLE_DECLARATION,
					"Method '%s' was declared twice in the same class '%s'",
					method.name, name);
		}

		methods.put(method.name, method);
	}
	
	public void removeMethod(MethodSymbol method) {
		checkArgument(method.getOwner().equals(this));
		checkArgument(methods.containsValue(method));
		methods.remove(method.name);
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