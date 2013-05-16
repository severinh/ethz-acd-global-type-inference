package cd.semantic.ti;

import java.util.Collection;

import cd.ir.symbols.ClassSymbol;
import cd.ir.symbols.MethodSymbol;
import cd.semantic.TypeSymbolTable;
import cd.semantic.ti.constraintSolving.ConstantTypeSetFactory;
import cd.semantic.ti.constraintSolving.ConstraintSystem;

public interface MethodConstraintGeneratorContext {

	public TypeSymbolTable getTypeSymbolTable();

	public ConstantTypeSetFactory getConstantTypeSetFactory();

	public ConstraintSystem getConstraintSystem();

	public Collection<MethodSymbol> getMatchingMethods(String name,
			int paramCount);

	public Collection<ClassSymbol> getClassesDeclaringField(String fieldName);

}
