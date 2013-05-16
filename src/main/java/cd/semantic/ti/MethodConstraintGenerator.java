package cd.semantic.ti;

import java.util.Collection;
import cd.ir.ast.MethodDecl;
import cd.ir.symbols.ClassSymbol;
import cd.ir.symbols.MethodSymbol;
import cd.semantic.TypeSymbolTable;
import cd.semantic.ti.constraintSolving.ConstantTypeSetFactory;
import cd.semantic.ti.constraintSolving.ConstraintSystem;

public abstract class MethodConstraintGenerator implements
		StmtConstraintGeneratorContext {

	private final MethodDecl methodDecl;
	private final MethodConstraintGeneratorContext context;

	public MethodConstraintGenerator(MethodDecl methodDecl,
			MethodConstraintGeneratorContext context) {
		super();

		this.methodDecl = methodDecl;
		this.context = context;
	}

	public MethodDecl getMethodDecl() {
		return methodDecl;
	}

	@Override
	public TypeSymbolTable getTypeSymbolTable() {
		return context.getTypeSymbolTable();
	}

	@Override
	public ConstantTypeSetFactory getConstantTypeSetFactory() {
		return context.getConstantTypeSetFactory();
	}

	@Override
	public ConstraintSystem getConstraintSystem() {
		return context.getConstraintSystem();
	}

	@Override
	public Collection<MethodSymbol> getMatchingMethods(String name,
			int paramCount) {
		return context.getMatchingMethods(name, paramCount);
	}

	@Override
	public Collection<ClassSymbol> getClassesDeclaringField(String fieldName) {
		return context.getClassesDeclaringField(fieldName);
	}

	@Override
	public MethodSymbol getMethod() {
		return methodDecl.sym;
	}

	public final void generate() {
		StmtConstraintGenerator generator = new StmtConstraintGenerator(this);
		getMethodDecl().accept(generator, null);
	}

}