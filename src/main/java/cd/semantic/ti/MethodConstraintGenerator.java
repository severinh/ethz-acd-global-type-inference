package cd.semantic.ti;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import cd.ir.ast.MethodDecl;
import cd.ir.symbols.ClassSymbol;
import cd.ir.symbols.MethodSymbol;
import cd.ir.symbols.TypeSymbol;
import cd.ir.symbols.VariableSymbol;
import cd.semantic.TypeSymbolTable;
import cd.semantic.ti.constraintSolving.ConstantTypeSet;
import cd.semantic.ti.constraintSolving.ConstantTypeSetFactory;
import cd.semantic.ti.constraintSolving.ConstraintSystem;
import cd.semantic.ti.constraintSolving.TypeSet;
import cd.semantic.ti.constraintSolving.TypeVariable;

/**
 * ConstraintGenerator is responsible for creating as many type variables and
 * constraints as necessary for a method.
 */
public class MethodConstraintGenerator implements ConstraintGenerationContext {

	private final TypeSymbolTable typeSymbols;
	private final MethodDecl mdecl;
	private final ConstraintSystem constraintSystem;

	private final MethodSymbolCache methodSymbolCache;
	private final ClassSymbolFieldCache classFieldSymbolCache;
	private final ConstantTypeSetFactory constantTypeSetFactory;

	private ConstantTypeSet allowedReturnTypeSet;

	// Map to remember the type variables for our parameters and locals,
	// i.e. what we are eventually interested in.
	// Note to avoid confusion: VariableSymbols are symbols for program
	// variables while
	// these TypeVariables are constraint solver variables describing the
	// type of such program variables
	private final Map<VariableSymbol, TypeSet> localSymbolVariables = new HashMap<>();

	public MethodConstraintGenerator(MethodDecl mdecl,
			TypeSymbolTable typeSymbols) {
		this.typeSymbols = typeSymbols;
		this.mdecl = mdecl;
		this.constraintSystem = new ConstraintSystem();
		this.methodSymbolCache = MethodSymbolCache.of(typeSymbols);
		this.classFieldSymbolCache = ClassSymbolFieldCache.of(typeSymbols);
		this.constantTypeSetFactory = new ConstantTypeSetFactory(typeSymbols);
	}

	@Override
	public ConstraintSystem getConstraintSystem() {
		return constraintSystem;
	}

	@Override
	public ConstantTypeSetFactory getConstantTypeSetFactory() {
		return constantTypeSetFactory;
	}

	@Override
	public Collection<MethodSymbol> getMatchingMethods(String name,
			int parameterCount) {
		return methodSymbolCache.get(name, parameterCount);
	}

	@Override
	public Collection<ClassSymbol> getClassesDeclaringField(String fieldName) {
		return classFieldSymbolCache.get(fieldName);
	}

	@Override
	public TypeSet getLocalVariableTypeSet(VariableSymbol localVariable) {
		return localSymbolVariables.get(localVariable);
	}

	@Override
	public TypeSet getReturnTypeSet() {
		return allowedReturnTypeSet;
	}

	@Override
	public MethodSymbol getCurrentMethod() {
		return mdecl.sym;
	}

	@Override
	public TypeSymbolTable getTypeSymbolTable() {
		return typeSymbols;
	}

	public Set<TypeSymbol> getPossibleTypes(VariableSymbol varSym) {
		return localSymbolVariables.get(varSym).getTypes();
	}

	public void generate() {
		// variables and constraints for parameters (types given!)
		MethodSymbol msym = mdecl.sym;
		for (VariableSymbol varSym : msym.getParameters()) {
			// Do not create a type variable for the parameter.
			// There is nothing to infer since the parameter type is fixed.
			// However, it is not correct to use a singleton type set
			// with only the declared type, because otherwise, assigning
			// a valid value of a subtype would not be possible.
			// Thus, use the constant set of all declarable subtypes.
			ConstantTypeSet typeConst = constantTypeSetFactory
					.makeDeclarableSubtypes(varSym.getType());
			localSymbolVariables.put(varSym, typeConst);
		}

		// type variables for local variables
		for (VariableSymbol varSym : msym.getLocals()) {
			TypeVariable typeVar = constraintSystem.addTypeVariable("local_"
					+ varSym.name);
			localSymbolVariables.put(varSym, typeVar);
		}

		// type variable and constraints for return value (if any)
		if (msym.returnType == typeSymbols.getVoidType()) {
			allowedReturnTypeSet = constantTypeSetFactory.makeEmpty();
		} else {
			allowedReturnTypeSet = constantTypeSetFactory
					.makeDeclarableSubtypes(msym.returnType);
		}

		StmtConstraintGenerator constraintVisitor = new StmtConstraintGenerator(
				this);
		mdecl.accept(constraintVisitor, null);
	}

}