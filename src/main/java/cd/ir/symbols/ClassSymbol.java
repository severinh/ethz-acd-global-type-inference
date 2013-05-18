package cd.ir.symbols;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.base.Optional;

import cd.exceptions.SemanticFailure;
import cd.exceptions.SemanticFailure.Cause;
import cd.semantic.SymbolTable;
import static com.google.common.base.Preconditions.checkArgument;

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

	// Third-party library is missing non-null annotations
	@SuppressWarnings("null")
	public ClassSymbol(String name, ClassSymbol superClass) {
		this(name, Optional.of(superClass));
	}

	public static ClassSymbol makeObjectClass() {
		// Third-party library is missing non-null annotations
		@SuppressWarnings("null")
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

	@Nullable
	public VariableSymbol getField(String name) {
		VariableSymbol fieldSymbol = fields.get(name);
		if (fieldSymbol == null && superClass.isPresent()) {
			return superClass.get().getField(name);
		}
		return fieldSymbol;
	}

	@Nullable
	public MethodSymbol getMethod(String name) {
		MethodSymbol methodSymbol = methods.get(name);
		if (methodSymbol == null && superClass.isPresent()) {
			return superClass.get().getMethod(name);
		}
		return methodSymbol;
	}

	/**
	 * Returns the list of fields declared in this class.
	 * 
	 * @return the list of fields, not including the ones of super-classes
	 */
	// Third-party library is missing non-null annotations
	@SuppressWarnings("null")
	public Collection<VariableSymbol> getDeclaredFields() {
		return Collections.unmodifiableCollection(fields.values());
	}

	/**
	 * Returns the list of methods declared in this class.
	 * 
	 * @return the list of methods, not including the ones of super-classes
	 */
	// Third-party library is missing non-null annotations
	@SuppressWarnings("null")
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