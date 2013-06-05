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
			// Use the bottom type if there are no types in the type
			// set. Since the constraint system has been solved
			// successfully, this usually (always?) means that the
			// variable symbol is not used at all.

			// TODO: The above is no true. if e.g. a field of an object is only
			// assigned to to the same field of another object of the same class
			// we do not have any constraints and therefore get bottom. This is
			// not ok, since it's relevant for the code generator!

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