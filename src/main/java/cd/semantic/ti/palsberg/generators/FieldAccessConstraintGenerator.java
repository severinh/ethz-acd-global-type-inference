package cd.semantic.ti.palsberg.generators;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableList;

import cd.ir.ast.Expr;
import cd.ir.ast.Field;
import cd.ir.symbols.ClassSymbol;
import cd.ir.symbols.VariableSymbol;
import cd.semantic.ti.palsberg.solving.TypeSet;

public class FieldAccessConstraintGenerator extends
		ReceiverConstraintGenerator<Field, ClassSymbol> {

	public FieldAccessConstraintGenerator(
			ExprConstraintGenerator exprConstraintGenerator, Field field) {
		super(exprConstraintGenerator, field);
	}

	@Override
	protected Expr getReceiver() {
		return ast.arg();
	}

	@Override
	protected List<? extends Expr> getArguments() {
		return ImmutableList.of();
	}

	@Override
	protected Set<ClassSymbol> getPossibleReceiverTypes() {
		Collection<ClassSymbol> declaringClassSymbols = generator.getContext()
				.getClassesDeclaringField(ast.fieldName);
		Set<ClassSymbol> possibleClassSymbols = new HashSet<>();
		for (ClassSymbol classSym : declaringClassSymbols) {
			possibleClassSymbols.addAll(generator.getTypeSymbols()
					.getClassSymbolSubtypes(classSym));
		}
		return possibleClassSymbols;
	}

	@Override
	protected List<? extends TypeSet> getParameterTypeSets(
			ClassSymbol receiverType) {
		return ImmutableList.of();
	}

	@Override
	protected TypeSet getResultTypeSet(ClassSymbol receiverType) {
		VariableSymbol fieldSymbol = receiverType.getField(ast.fieldName);
		return generator.getContext().getVariableTypeSet(fieldSymbol);
	}

}
