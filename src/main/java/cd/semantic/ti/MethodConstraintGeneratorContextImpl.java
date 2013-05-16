package cd.semantic.ti;

import java.util.Collection;

import cd.ir.symbols.ClassSymbol;
import cd.ir.symbols.MethodSymbol;
import cd.semantic.TypeSymbolTable;
import cd.semantic.ti.constraintSolving.ConstantTypeSetFactory;
import cd.semantic.ti.constraintSolving.ConstraintSystem;

public class MethodConstraintGeneratorContextImpl implements
		MethodConstraintGeneratorContext {

	private final TypeSymbolTable typeSymbols;
	private final ConstantTypeSetFactory constantTypeSetFactory;
	private final ConstraintSystem constraintSystem;
	private final MethodSymbolCache methodSymbolCache;
	private final ClassSymbolFieldCache classSymbolFieldCache;

	public MethodConstraintGeneratorContextImpl(TypeSymbolTable typeSymbols) {
		this.typeSymbols = typeSymbols;
		this.constantTypeSetFactory = new ConstantTypeSetFactory(typeSymbols);
		this.constraintSystem = new ConstraintSystem();
		this.methodSymbolCache = MethodSymbolCache.of(typeSymbols);
		this.classSymbolFieldCache = ClassSymbolFieldCache.of(typeSymbols);
	}

	@Override
	public TypeSymbolTable getTypeSymbolTable() {
		return typeSymbols;
	}

	@Override
	public ConstantTypeSetFactory getConstantTypeSetFactory() {
		return constantTypeSetFactory;
	}

	@Override
	public ConstraintSystem getConstraintSystem() {
		return constraintSystem;
	}

	@Override
	public Collection<MethodSymbol> getMatchingMethods(String name,
			int paramCount) {
		return methodSymbolCache.get(name, paramCount);
	}

	@Override
	public Collection<ClassSymbol> getClassesDeclaringField(String fieldName) {
		return classSymbolFieldCache.get(fieldName);
	}

}
