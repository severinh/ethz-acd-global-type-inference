package cd.semantic.ti;

import java.util.Set;

import cd.exceptions.SemanticFailure;
import cd.exceptions.SemanticFailure.Cause;
import cd.ir.ast.MethodDecl;
import cd.ir.symbols.TypeSymbol;
import cd.ir.symbols.VariableSymbol;
import cd.semantic.TypeSymbolTable;
import cd.semantic.ti.constraintSolving.ConstraintSolver;
import cd.semantic.ti.constraintSolving.ConstraintSystem;

public class LocalTypeInferenceWithConstraints extends LocalTypeInference {

	@Override
	public void inferTypes(MethodDecl mdecl, TypeSymbolTable typeSymbols) {
		MethodConstraintGenerator generator = new MethodConstraintGenerator(
				mdecl, typeSymbols);
		generator.generate();
		ConstraintSystem constraintSystem = generator.getConstraintSystem();
		ConstraintSolver solver = new ConstraintSolver(constraintSystem);
		solver.solve();
		if (!solver.hasSolution()) {
			throw new SemanticFailure(Cause.TYPE_INFERENCE_ERROR,
					"Type inference was unable to resolve type constraints");
		} else {
			for (VariableSymbol varSym : mdecl.sym.getLocals()) {
				Set<TypeSymbol> possibleTypes = generator
						.getPossibleTypes(varSym);
				TypeSymbol type = null;
				if (possibleTypes.isEmpty()) {
					// Use the bottom type if there are no types in the type
					// set. Since the constraint system has been solved
					// successfully, this usually (always?) means that the
					// variable symbol is not used at all.
					type = typeSymbols.getBottomType();
				} else if (possibleTypes.size() == 1) {
					type = possibleTypes.iterator().next();
				} else if (possibleTypes.size() > 1) {
					// NOTE: we may still try to take the join (lca). This is
					// sometimes necessary.
					TypeSymbol[] typesArray = possibleTypes
							.toArray(new TypeSymbol[possibleTypes.size()]);
					TypeSymbol lca = typeSymbols.getLCA(typesArray);
					if (lca != typeSymbols.getTopType()) {
						type = lca;
					} else {
						throw new SemanticFailure(Cause.TYPE_INFERENCE_ERROR,
								"Type inference resulted in ambiguous type for "
										+ varSym.name);
					}
				}
				varSym.setType(type);
			}
		}

	}

}
