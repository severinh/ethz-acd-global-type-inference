package cd.semantic.ti.palsberg;

import javax.annotation.Nonnull;

import cd.CompilationContext;
import cd.exceptions.SemanticFailure;
import cd.exceptions.SemanticFailure.Cause;
import cd.ir.ast.ClassDecl;
import cd.ir.ast.MethodDecl;
import cd.ir.symbols.TypeSymbol;
import cd.ir.symbols.VariableSymbol;
import cd.semantic.TypeSymbolTable;
import cd.semantic.ti.palsberg.generators.LocalConstraintGeneratorContext;
import cd.semantic.ti.palsberg.generators.MethodConstraintGenerator;
import cd.semantic.ti.palsberg.solving.ConstraintSolver;
import cd.semantic.ti.palsberg.solving.TypeSet;

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
		LocalConstraintGeneratorContext context = LocalConstraintGeneratorContext
				.of(typeSymbols, mdecl.sym);
		MethodConstraintGenerator generator = new MethodConstraintGenerator(
				mdecl, context);
		generator.generate();
		ConstraintSolver solver = new ConstraintSolver(typeSymbols,
				context.getConstraintSystem());
		solver.solve();
		if (!solver.hasSolution()) {
			throw new SemanticFailure(Cause.TYPE_INFERENCE_ERROR,
					"Type inference was unable to resolve type constraints");
		} else {
			for (VariableSymbol varSym : mdecl.sym.getLocals()) {
				TypeSet typeSet = context.getVariableTypeSet(varSym);
				TypeSymbol type = makeStaticType(typeSymbols, typeSet,
						varSym.name);
				varSym.setType(type);
			}
		}

	}
}
