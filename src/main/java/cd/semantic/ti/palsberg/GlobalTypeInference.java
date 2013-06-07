package cd.semantic.ti.palsberg;

import java.util.Map.Entry;

import javax.annotation.Nullable;

import cd.CompilationContext;
import cd.exceptions.SemanticFailure;
import cd.exceptions.SemanticFailure.Cause;
import cd.ir.AstVisitor;
import cd.ir.ast.ClassDecl;
import cd.ir.ast.MethodDecl;
import cd.ir.ast.ReturnStmt;
import cd.ir.symbols.MethodSymbol;
import cd.ir.symbols.TypeSymbol;
import cd.ir.symbols.VariableSymbol;
import cd.semantic.TypeSymbolTable;
import cd.semantic.ti.palsberg.generators.GlobalConstraintGeneratorContext;
import cd.semantic.ti.palsberg.generators.MethodConstraintGenerator;
import cd.semantic.ti.palsberg.solving.ConstraintSolver;
import cd.semantic.ti.palsberg.solving.TypeVariable;
import cd.util.NonnullByDefault;

public class GlobalTypeInference extends TypeInferenceWithConstraints {

	@Override
	public void inferTypes(CompilationContext compilationContext) {
		TypeSymbolTable typeSymbols = compilationContext.getTypeSymbols();
		GlobalConstraintGeneratorContext generatorContext = GlobalConstraintGeneratorContext
				.of(typeSymbols);

		for (ClassDecl classDecl : compilationContext.getAstRoots()) {
			for (MethodDecl methodDecl : classDecl.methods()) {
				MethodConstraintGenerator generator = new MethodConstraintGenerator(
						methodDecl, generatorContext);
				generator.generate();
			}
		}

		ConstraintSolver solver = new ConstraintSolver(typeSymbols,
				generatorContext.getConstraintSystem());
		solver.solve();
		if (!solver.hasSolution()) {
			throw new SemanticFailure(Cause.TYPE_INFERENCE_ERROR,
					"Type inference was unable to resolve type constraints");
		} else {
			for (Entry<VariableSymbol, TypeVariable> entry : generatorContext
					.getVariableSymbolTypeSets().entrySet()) {
				VariableSymbol variableSymbol = entry.getKey();
				TypeVariable typeSet = entry.getValue();
				TypeSymbol type = makeStaticType(typeSymbols, typeSet,
						variableSymbol.name);
				variableSymbol.setType(type);
			}

			for (Entry<MethodSymbol, TypeVariable> entry : generatorContext
					.getReturnTypeSets().entrySet()) {
				MethodSymbol method = entry.getKey();
				TypeVariable typeSet = entry.getValue();
				TypeSymbol type;
				if (typeSet.getTypes().isEmpty()) {
					// If any return statement has an expression, we may NOT infer void.
					// An empty return type variable then indicates a problem.
					ExprReturnVisitor returnVisitor = new ExprReturnVisitor();
					MethodDecl mdecl = compilationContext.getMethodDecl(method);
					if (returnVisitor.hasExprReturn(mdecl)) {
						throw new SemanticFailure(Cause.TYPE_ERROR,
								"Method returning an expression was inferred to have bottom type");					
					} else {
						type = typeSymbols.getVoidType();

					}
				} else {
					type = makeStaticType(typeSymbols, typeSet, "return");
				}
				method.returnType = type;
			}
		}
	}

	public final class ExprReturnVisitor extends AstVisitor<Void, Void> {
		boolean exprReturnStmt = false;
	
		@Override
		public @Nullable Void returnStmt(ReturnStmt ast, Void arg) {
			if (ast.arg() != null) {
				exprReturnStmt = true;
			}
			return null;
		}
	
		public boolean hasExprReturn(MethodDecl mdecl) {
			visit(mdecl, null);
			return exprReturnStmt;
		}
	}
}
