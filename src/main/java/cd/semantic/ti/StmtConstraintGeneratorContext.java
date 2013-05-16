package cd.semantic.ti;

import cd.ir.symbols.MethodSymbol;
import cd.ir.symbols.VariableSymbol;
import cd.semantic.ti.constraintSolving.TypeSet;

public interface StmtConstraintGeneratorContext extends
		MethodConstraintGeneratorContext {

	public TypeSet getLocalVariableTypeSet(VariableSymbol localVariable);

	public TypeSet getParameterTypeSet(VariableSymbol parameter);

	public TypeSet getFieldTypeSet(VariableSymbol field);

	public TypeSet getReturnTypeSet();

	public MethodSymbol getMethod();

}
