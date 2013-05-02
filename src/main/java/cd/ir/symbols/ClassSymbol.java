package cd.ir.symbols;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import cd.exceptions.SemanticFailure;
import cd.exceptions.SemanticFailure.Cause;
import cd.ir.ast.ClassDecl;

public class ClassSymbol extends TypeSymbol {

	private ClassSymbol superClass;
	public final VariableSymbol thisSymbol = new VariableSymbol("this", this);

	private final Map<String, VariableSymbol> fields = new LinkedHashMap<>();
	private final Map<String, MethodSymbol> methods = new LinkedHashMap<>();

	public int totalMethods = -1;
	public int totalFields = -1;
	public int sizeof = -1;

	public ClassSymbol(ClassDecl classDecl) {
		super(classDecl.name);
	}

	/**
	 * Used to create the default {@code Object} and {@code <null>} types
	 */
	public ClassSymbol(String name) {
		super(name);
	}

	public ClassSymbol getSuperClass() {
		return superClass;
	}

	public void setSuperClass(ClassSymbol superClass) {
		this.superClass = superClass;
	}

	@Override
	public boolean isReferenceType() {
		return true;
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
		if (fields.containsKey(field.name)) {
			throw new SemanticFailure(Cause.DOUBLE_DECLARATION,
					"Field '%s' was declared twice in the same class '%s'",
					field.name, name);
		}

		fields.put(field.name, field);
	}

}