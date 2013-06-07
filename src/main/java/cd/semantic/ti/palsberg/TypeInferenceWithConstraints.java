package cd.semantic.ti.palsberg;

import java.util.Set;

import cd.exceptions.SemanticFailure;
import cd.exceptions.SemanticFailure.Cause;
import cd.ir.symbols.TypeSymbol;
import cd.semantic.TypeSymbolTable;
import cd.semantic.ti.TypeInference;
import cd.semantic.ti.palsberg.solving.TypeSet;

/**
 * Base class for type inference implementations based on constraint solving.
 */
public abstract class TypeInferenceWithConstraints implements TypeInference {

	public TypeSymbol makeStaticType(TypeSymbolTable typeSymbols,
			TypeSet typeSet, String name) {
		Set<TypeSymbol> possibleTypes = typeSet.getTypes();
		TypeSymbol type = null;
		if (possibleTypes.isEmpty()) {
			// Use the bottom type if there are no types in the type set.
			// This may happen if no constraint that imposes a lower bound on
			// the type variable, e.g., when the variable is never used. We
			// cannot choose an arbitrary type such as Object though because
			// there may still be upper bounds that are violated otherwise.
			type = typeSymbols.getBottomType();
		} else if (possibleTypes.size() == 1) {
			type = possibleTypes.iterator().next();
		} else if (possibleTypes.size() > 1) {
			if (possibleTypes.contains(typeSymbols.getTopType())) {
				throw new SemanticFailure(Cause.TYPE_INFERENCE_ERROR,
						"Type inference resulted in ambiguous type for " + name);
			} else {
				type = typeSymbols.getLCA(possibleTypes);
			}
		}
		return type;
	}
}
