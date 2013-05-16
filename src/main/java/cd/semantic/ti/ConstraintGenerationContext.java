package cd.semantic.ti;

import java.util.Collection;

import cd.ir.symbols.ClassSymbol;
import cd.ir.symbols.MethodSymbol;
import cd.ir.symbols.VariableSymbol;
import cd.semantic.TypeSymbolTable;
import cd.semantic.ti.constraintSolving.ConstantTypeSetFactory;
import cd.semantic.ti.constraintSolving.ConstraintSystem;
import cd.semantic.ti.constraintSolving.TypeSet;

public interface ConstraintGenerationContext {

	public ConstantTypeSetFactory getConstantTypeSetFactory();

	public ConstraintSystem getConstraintSystem();

	public Collection<MethodSymbol> getMatchingMethods(String name,
			int paramCount);

	public Collection<ClassSymbol> getClassesDeclaringField(String fieldName);

	public TypeSet getLocalVariableTypeSet(VariableSymbol localVariable);

	public MethodSymbol getCurrentMethod();

	public TypeSymbolTable getTypeSymbolTable();

}
