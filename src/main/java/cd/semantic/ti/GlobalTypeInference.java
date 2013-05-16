package cd.semantic.ti;

import cd.CompilationContext;
import cd.semantic.TypeSymbolTable;
import cd.semantic.ti.LocalTypeInferenceWithConstraints.ConstraintGenerator;
import cd.semantic.ti.constraintSolving.ConstraintSolver;
import cd.semantic.ti.constraintSolving.ConstraintSystem;
import cd.util.NonnullByDefault;

@NonnullByDefault
public class GlobalTypeInference implements TypeInference {

	@Override
	public void inferTypes(CompilationContext context) {
	}

}
