package cd.semantic.ti;

import java.util.HashMap;
import java.util.Map;

import cd.ir.ast.MethodDecl;
import cd.ir.symbols.MethodSymbol;
import cd.ir.symbols.TypeSymbol;
import cd.ir.symbols.VariableSymbol;
import cd.semantic.ti.constraintSolving.TypeSet;
import cd.semantic.ti.constraintSolving.TypeVariable;

/**
 * ConstraintGenerator is responsible for creating as many type variables and
 * constraints as necessary for a method.
 */
public class LocalMethodConstraintGenerator extends MethodConstraintGenerator {

	// Map to remember the type variables for our parameters and locals,
	// i.e. what we are eventually interested in.
	// Note to avoid confusion: VariableSymbols are symbols for program
	// variables while these TypeVariables are constraint solver variables
	// describing the type of such program variables
	private final Map<VariableSymbol, TypeSet> localVariableTypeSets;

	public LocalMethodConstraintGenerator(MethodDecl methodDecl,
			MethodConstraintGeneratorContext context) {
		super(methodDecl, context);

		this.localVariableTypeSets = new HashMap<>();
		for (VariableSymbol varSym : getMethod().getLocals()) {
			TypeVariable typeVar = getConstraintSystem().addTypeVariable(
					"local_" + varSym.name);
			localVariableTypeSets.put(varSym, typeVar);
		}
	}

	@Override
	public TypeSet getVariableTypeSet(VariableSymbol symbol) {
		switch (symbol.getKind()) {
		case FIELD:
		case PARAM:
			TypeSymbol type = symbol.getType();
			return getConstantTypeSetFactory().makeDeclarableSubtypes(type);
		case LOCAL:
			return localVariableTypeSets.get(symbol);
		default:
			throw new IllegalArgumentException("no such variable kind");
		}
	}

	@Override
	public TypeSet getReturnTypeSet(MethodSymbol method) {
		TypeSymbol type = method.returnType;
		if (type == getTypeSymbolTable().getVoidType()) {
			return getConstantTypeSetFactory().makeEmpty();
		} else {
			return getConstantTypeSetFactory().makeDeclarableSubtypes(type);
		}
	}

}