package cd.semantic.ti;

import cd.ir.symbols.MethodSymbol;
import cd.ir.symbols.TypeSymbol;
import cd.ir.symbols.VariableSymbol;
import cd.semantic.TypeSymbolTable;
import cd.semantic.ti.constraintSolving.TypeSet;

public final class LocalConstraintGeneratorContext extends
		ConstraintGeneratorContext {

	private LocalConstraintGeneratorContext(TypeSymbolTable typeSymbols) {
		super(typeSymbols);
	}

	public static LocalConstraintGeneratorContext of(
			TypeSymbolTable typeSymbols, MethodSymbol methodSymbol) {
		LocalConstraintGeneratorContext result = new LocalConstraintGeneratorContext(
				typeSymbols);
		for (VariableSymbol variable : methodSymbol.getLocalsAndParameters()) {
			String desc = variable.getName();
			result.addVariableTypeSet(variable, desc);
		}
		return result;
	}

	@Override
	public TypeSet getVariableTypeSet(VariableSymbol symbol) {
		switch (symbol.getKind()) {
		case FIELD:
		case PARAM:
			TypeSymbol type = symbol.getType();
			return getConstantTypeSetFactory().makeDeclarableSubtypes(type);
		case LOCAL:
			return super.getVariableTypeSet(symbol);
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
