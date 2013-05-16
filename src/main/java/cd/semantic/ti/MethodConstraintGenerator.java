package cd.semantic.ti;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import cd.ir.ast.MethodDecl;
import cd.ir.symbols.ClassSymbol;
import cd.ir.symbols.MethodSymbol;
import cd.ir.symbols.VariableSymbol;
import cd.semantic.TypeSymbolTable;
import cd.semantic.ti.constraintSolving.ConstantTypeSetFactory;
import cd.semantic.ti.constraintSolving.ConstraintSystem;
import cd.semantic.ti.constraintSolving.TypeSet;
import cd.semantic.ti.constraintSolving.TypeVariable;

public abstract class MethodConstraintGenerator implements
		StmtConstraintGeneratorContext {

	private final MethodDecl methodDecl;
	private final MethodConstraintGeneratorContext context;

	// Map to remember the type variables for our parameters and locals,
	// i.e. what we are eventually interested in.
	// Note to avoid confusion: VariableSymbols are symbols for program
	// variables while these TypeVariables are constraint solver variables
	// describing the type of such program variables
	private final Map<VariableSymbol, TypeSet> localVariableTypeSets;

	public MethodConstraintGenerator(MethodDecl methodDecl,
			MethodConstraintGeneratorContext context) {
		super();

		this.methodDecl = methodDecl;
		this.context = context;
		this.localVariableTypeSets = new HashMap<>();

		for (VariableSymbol varSym : getMethod().getLocals()) {
			TypeVariable typeVar = getConstraintSystem().addTypeVariable(
					"local_" + varSym.name);
			localVariableTypeSets.put(varSym, typeVar);
		}
	}

	public MethodDecl getMethodDecl() {
		return methodDecl;
	}

	@Override
	public TypeSymbolTable getTypeSymbolTable() {
		return context.getTypeSymbolTable();
	}

	@Override
	public ConstantTypeSetFactory getConstantTypeSetFactory() {
		return context.getConstantTypeSetFactory();
	}

	@Override
	public ConstraintSystem getConstraintSystem() {
		return context.getConstraintSystem();
	}

	@Override
	public Collection<MethodSymbol> getMatchingMethods(String name,
			int paramCount) {
		return context.getMatchingMethods(name, paramCount);
	}

	@Override
	public Collection<ClassSymbol> getClassesDeclaringField(String fieldName) {
		return context.getClassesDeclaringField(fieldName);
	}

	@Override
	public MethodSymbol getMethod() {
		return methodDecl.sym;
	}

	@Override
	public TypeSet getLocalVariableTypeSet(VariableSymbol localVariable) {
		return localVariableTypeSets.get(localVariable);
	}

	public final void generate() {
		StmtConstraintGenerator generator = new StmtConstraintGenerator(this);
		getMethodDecl().accept(generator, null);
	}

}