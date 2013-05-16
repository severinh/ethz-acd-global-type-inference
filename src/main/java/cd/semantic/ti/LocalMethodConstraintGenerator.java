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

	private final LocalTypeVariableStore typeVariableStore;

	public LocalMethodConstraintGenerator(MethodDecl methodDecl,
			MethodConstraintGeneratorContext context,
			LocalTypeVariableStore localTypeVariableStore) {
		super(methodDecl, context);

		this.typeVariableStore = localTypeVariableStore;
	}

	@Override
	public TypeSet getVariableTypeSet(VariableSymbol symbol) {
		switch (symbol.getKind()) {
		case FIELD:
		case PARAM:
			TypeSymbol type = symbol.getType();
			return getConstantTypeSetFactory().makeDeclarableSubtypes(type);
		case LOCAL:
			return typeVariableStore.getVariableSymbolTypeSet(symbol);
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