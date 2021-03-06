package cd;

import cd.semantic.ti.LocalTypeInferenceLightweight;
import cd.semantic.ti.TypeInference;
import cd.semantic.ti.palsberg.GlobalTypeInference;
import cd.semantic.ti.palsberg.LocalTypeInferenceWithConstraints;

/**
 * Specifies the scope of type inference, if any, and provides clients with the
 * corresponding implementation of the {@link TypeInference} interface.
 * 
 * @note should it turn out that this couples type inference modes too tightly
 *       to implementations, one may move the logic out of the enum again
 */
public enum TypeInferenceMode {
	NONE {
		@Override
		public TypeInference getTypeInference() {
			return new TypeInference() {
				@Override
				public void inferTypes(CompilationContext context) {
					// nop
				}
			};
		}
	},
	LOCAL_LIGHTWEIGHT {
		@Override
		public TypeInference getTypeInference() {
			return new LocalTypeInferenceLightweight();
		}
	},
	LOCAL_CONSTRAINTS {
		@Override
		public TypeInference getTypeInference() {
			return new LocalTypeInferenceWithConstraints();
		}
	},
	GLOBAL {
		@Override
		public TypeInference getTypeInference() {
			return new GlobalTypeInference();
		}
	};

	/**
	 * Returns the {@link TypeInference} implementation corresponding to a given
	 * type erasure mode.
	 * 
	 * @return the concrete type inference
	 */
	public abstract TypeInference getTypeInference();

}