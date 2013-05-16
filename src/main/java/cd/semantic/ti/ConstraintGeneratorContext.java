package cd.semantic.ti;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import cd.ir.symbols.ClassSymbol;
import cd.ir.symbols.MethodSymbol;
import cd.ir.symbols.VariableSymbol;
import cd.semantic.TypeSymbolTable;
import cd.semantic.ti.constraintSolving.ConstantTypeSetFactory;
import cd.semantic.ti.constraintSolving.ConstraintSystem;
import cd.semantic.ti.constraintSolving.TypeSet;
import cd.semantic.ti.constraintSolving.TypeVariable;

/**
 * Represents the context that the constraint generation for type inference
 * types place in. It can be used for both global and local type inference.
 * 
 * It provides the constraint system, caches various values and also holds the
 * mapping of type variables back to variable symbols.
 * 
 * The context is not associated with a fixed method symbol deliberately,
 * because in global type inference, the same context is used for all methods.
 */
public abstract class ConstraintGeneratorContext {

	private final TypeSymbolTable typeSymbols;
	private final ConstantTypeSetFactory constantTypeSetFactory;
	private final ConstraintSystem constraintSystem;
	private final MethodSymbolCache methodSymbolCache;
	private final ClassSymbolFieldCache classSymbolFieldCache;

	protected final Map<VariableSymbol, TypeVariable> variableSymbolTypeSets;

	public ConstraintGeneratorContext(TypeSymbolTable typeSymbols) {
		this.typeSymbols = typeSymbols;
		this.constantTypeSetFactory = new ConstantTypeSetFactory(typeSymbols);
		this.constraintSystem = new ConstraintSystem();
		this.methodSymbolCache = MethodSymbolCache.of(typeSymbols);
		this.classSymbolFieldCache = ClassSymbolFieldCache.of(typeSymbols);

		this.variableSymbolTypeSets = new HashMap<>();
	}

	public TypeSymbolTable getTypeSymbolTable() {
		return typeSymbols;
	}

	public ConstantTypeSetFactory getConstantTypeSetFactory() {
		return constantTypeSetFactory;
	}

	public ConstraintSystem getConstraintSystem() {
		return constraintSystem;
	}

	public Collection<MethodSymbol> getMatchingMethods(String name,
			int paramCount) {
		return methodSymbolCache.get(name, paramCount);
	}

	public Collection<ClassSymbol> getClassesDeclaringField(String fieldName) {
		return classSymbolFieldCache.get(fieldName);
	}

	public Map<VariableSymbol, TypeVariable> getVariableSymbolTypeSets() {
		return Collections.unmodifiableMap(variableSymbolTypeSets);
	}

	/**
	 * Returns the type set associated with a given variable symbol.
	 * 
	 * By default, it looks up the type variable associated with the given
	 * variable symbol in this context. However, subclasses may override this
	 * behavior such that the method returns constant type sets or certain kinds
	 * of variables such as fields or parameters.
	 * 
	 * @param variable
	 *            can be any local variable, parameter and field
	 * @return a variable or constant set of types
	 */
	public TypeSet getVariableTypeSet(VariableSymbol variable) {
		return variableSymbolTypeSets.get(variable);
	}

	/**
	 * Create a new type variable for the given variable symbol, annotate it
	 * with a description and add it to the constraint system.
	 * 
	 * This method is only meant to be called during the construction process of
	 * the constraint generator context.
	 * 
	 * @param variable
	 *            the variable to create a type variable for
	 * @param desc
	 *            string that helps identify the variable symbol in the
	 *            constraint system
	 */
	protected void addVariableTypeSet(VariableSymbol variable, String desc) {
		TypeVariable typeSet = constraintSystem.addTypeVariable(desc);
		variableSymbolTypeSets.put(variable, typeSet);
	}

	public abstract TypeSet getReturnTypeSet(MethodSymbol method);

}
