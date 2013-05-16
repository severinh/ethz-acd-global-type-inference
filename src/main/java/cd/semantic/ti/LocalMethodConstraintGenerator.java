package cd.semantic.ti;

import cd.ir.ast.MethodDecl;
import cd.ir.symbols.MethodSymbol;
import cd.ir.symbols.TypeSymbol;
import cd.ir.symbols.VariableSymbol;
import cd.semantic.ti.constraintSolving.TypeSet;

/**
 * ConstraintGenerator is responsible for creating as many type variables and
 * constraints as necessary for a method.
 */
public class LocalMethodConstraintGenerator extends MethodConstraintGenerator {

	public LocalMethodConstraintGenerator(MethodDecl methodDecl,
			MethodConstraintGeneratorContext context) {
		super(methodDecl, context);
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

	@Override
	public TypeSet getParameterTypeSet(VariableSymbol parameter) {
		// Do not create a type variable for the parameter. There is nothing to
		// infer since the parameter type is fixed. However, it is not correct
		// to use a singleton type set with only the declared type, because
		// otherwise, assigning a valid value of a subtype would not be
		// possible. Thus, use the constant set of all declarable subtypes.

		TypeSymbol type = parameter.getType();
		return getConstantTypeSetFactory().makeDeclarableSubtypes(type);
	}

	@Override
	public TypeSet getFieldTypeSet(VariableSymbol field) {
		TypeSymbol type = field.getType();
		return getConstantTypeSetFactory().makeDeclarableSubtypes(type);
	}

}