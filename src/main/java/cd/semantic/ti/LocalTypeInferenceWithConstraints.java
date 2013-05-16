package cd.semantic.ti;

import javax.annotation.Nonnull;

import cd.CompilationContext;
import cd.exceptions.SemanticFailure;
import cd.exceptions.SemanticFailure.Cause;
import cd.ir.ast.ClassDecl;
import cd.ir.ast.MethodDecl;
import cd.ir.symbols.TypeSymbol;
import cd.ir.symbols.VariableSymbol;
import cd.semantic.TypeSymbolTable;
import cd.semantic.ti.constraintSolving.ConstraintSolver;
import cd.semantic.ti.constraintSolving.ConstraintSystem;
import cd.semantic.ti.constraintSolving.TypeSet;

public class LocalTypeInferenceWithConstraints extends
		TypeInferenceWithConstraints {

	@Override
	public void inferTypes(@Nonnull CompilationContext context) {
		TypeSymbolTable typeSymbols = context.getTypeSymbols();
		for (ClassDecl classDecl : context.getAstRoots()) {
			for (final MethodDecl mdecl : classDecl.methods()) {
				inferTypes(mdecl, typeSymbols);
			}
		}
	}

	public void inferTypes(MethodDecl mdecl, TypeSymbolTable typeSymbols) {
		MethodConstraintGeneratorContext context = new MethodConstraintGeneratorContextImpl(
				typeSymbols);
		LocalMethodConstraintGenerator generator = new LocalMethodConstraintGenerator(
				mdecl, context);
		generator.generate();
		ConstraintSystem constraintSystem = generator.getConstraintSystem();
		ConstraintSolver solver = new ConstraintSolver(constraintSystem);
		solver.solve();
		if (!solver.hasSolution()) {
			throw new SemanticFailure(Cause.TYPE_INFERENCE_ERROR,
					"Type inference was unable to resolve type constraints");
		} else {
			for (VariableSymbol varSym : mdecl.sym.getLocals()) {
				TypeSet typeSet = generator.getVariableTypeSet(varSym);
				TypeSymbol type = makeStaticType(typeSymbols, typeSet,
						varSym.name);
				varSym.setType(type);
			}
		}

	}

}
