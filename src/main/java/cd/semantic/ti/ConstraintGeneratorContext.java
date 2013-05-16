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

	public TypeSet getVariableTypeSet(VariableSymbol variable) {
		return variableSymbolTypeSets.get(variable);
	}

	public abstract TypeSet getReturnTypeSet(MethodSymbol method);

}
