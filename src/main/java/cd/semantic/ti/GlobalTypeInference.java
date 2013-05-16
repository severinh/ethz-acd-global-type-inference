package cd.semantic.ti;

import java.util.Map.Entry;

import cd.CompilationContext;
import cd.exceptions.SemanticFailure;
import cd.exceptions.SemanticFailure.Cause;
import cd.ir.ast.ClassDecl;
import cd.ir.ast.MethodDecl;
import cd.ir.symbols.MethodSymbol;
import cd.ir.symbols.TypeSymbol;
import cd.ir.symbols.VariableSymbol;
import cd.semantic.TypeSymbolTable;
import cd.semantic.ti.constraintSolving.ConstraintSolver;
import cd.semantic.ti.constraintSolving.ConstraintSystem;
import cd.semantic.ti.constraintSolving.TypeVariable;
import cd.util.NonnullByDefault;

@NonnullByDefault
public class GlobalTypeInference extends TypeInferenceWithConstraints {

	@Override
	public void inferTypes(CompilationContext compilationContext) {
		TypeSymbolTable typeSymbols = compilationContext.getTypeSymbols();
		MethodConstraintGeneratorContext generatorContext = new MethodConstraintGeneratorContextImpl(
				typeSymbols);
		ConstraintSystem constraintSystem = generatorContext
				.getConstraintSystem();
		GlobalTypeVariableStore typeVariableStore = GlobalTypeVariableStore.of(
				typeSymbols, constraintSystem);

		for (ClassDecl classDecl : compilationContext.getAstRoots()) {
			for (MethodDecl methodDecl : classDecl.methods()) {
				GlobalMethodConstraintGenerator generator = new GlobalMethodConstraintGenerator(
						methodDecl, generatorContext, typeVariableStore);
				generator.generate();
			}
		}

		ConstraintSolver solver = new ConstraintSolver(constraintSystem);
		solver.solve();
		if (!solver.hasSolution()) {
			throw new SemanticFailure(Cause.TYPE_INFERENCE_ERROR,
					"Type inference was unable to resolve type constraints");
		} else {
			for (Entry<VariableSymbol, TypeVariable> entry : typeVariableStore
					.getVariableSymbolTypeSets().entrySet()) {
				VariableSymbol variableSymbol = entry.getKey();
				TypeVariable typeSet = entry.getValue();
				TypeSymbol type = makeStaticType(typeSymbols, typeSet,
						variableSymbol.name);
				variableSymbol.setType(type);
			}

			for (Entry<MethodSymbol, TypeVariable> entry : typeVariableStore
					.getReturnTypeSets().entrySet()) {
				MethodSymbol method = entry.getKey();
				TypeVariable typeSet = entry.getValue();
				TypeSymbol type;
				if (typeSet.getTypes().isEmpty()) {
					type = typeSymbols.getVoidType();
				} else {
					type = makeStaticType(typeSymbols, typeSet, "return");
				}
				method.returnType = type;
			}
		}
	}

}
