package cd.semantic.ti;

import cd.ir.symbols.MethodSymbol;
import cd.ir.symbols.VariableSymbol;
import cd.semantic.ti.constraintSolving.TypeSet;

public interface StmtConstraintGeneratorContext extends
		MethodConstraintGeneratorContext {

	public TypeSet getVariableTypeSet(VariableSymbol localVariable);

	public TypeSet getReturnTypeSet(MethodSymbol method);

	public MethodSymbol getMethod();

}
