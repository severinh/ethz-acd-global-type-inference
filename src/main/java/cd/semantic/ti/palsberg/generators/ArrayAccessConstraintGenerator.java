package cd.semantic.ti.palsberg.generators;

import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableList;

import cd.ir.ast.Expr;
import cd.ir.ast.Index;
import cd.ir.symbols.ArrayTypeSymbol;
import cd.semantic.ti.palsberg.solving.TypeSet;

/**
 * Generates conditional constraints for an array access.
 * 
 * @see ReceiverConstraintGenerator
 */
public class ArrayAccessConstraintGenerator extends
		ReceiverConstraintGenerator<Index, ArrayTypeSymbol> {

	public ArrayAccessConstraintGenerator(
			ExprConstraintGenerator exprConstraintGenerator, Index ast) {
		super(exprConstraintGenerator, ast);
	}

	@Override
	protected Expr getReceiver() {
		return ast.left();
	}

	@Override
	protected List<? extends Expr> getArguments() {
		return ImmutableList.of(ast.right());
	}

	@Override
	protected Set<ArrayTypeSymbol> getPossibleReceiverTypes() {
		return generator.getTypeSymbols().getArrayTypeSymbols();
	}

	@Override
	protected List<? extends TypeSet> getParameterTypeSets(
			ArrayTypeSymbol receiverType) {
		return ImmutableList.of(generator.getTypeSetFactory().makeInt());
	}

	@Override
	protected TypeSet getResultTypeSet(ArrayTypeSymbol receiverType) {
		// Also allow objects in the array whose type is a subtype of the
		// declared array element type
		return generator.getTypeSetFactory().makeSubtypes(
				receiverType.elementType);
	}

}
