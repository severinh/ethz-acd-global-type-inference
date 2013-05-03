package cd;

import cd.semantic.TypeSymbolTable;
import cd.semantic.ti.GlobalTypeEraser;
import cd.semantic.ti.LocalTypeEraser;
import cd.semantic.ti.TypeEraser;

/**
 * Specifies the scope of type erasure, if any, and provides clients with the
 * corresponding implementation of the {@link TypeEraser} interface.
 * 
 * @note should it turn out that this couples type erasure modes too tightly to
 *       implementations, one may move the logic out of the enum again
 */
public enum TypeErasureMode {
	NONE {
		@Override
		public TypeEraser getTypeEraser() {
			return new TypeEraser() {
				@Override
				public void eraseTypesFrom(TypeSymbolTable symbolTable) {
					// nop
				}
			};
		}
	},
	LOCAL {
		@Override
		public TypeEraser getTypeEraser() {
			return LocalTypeEraser.getInstance();
		}
	},
	GLOBAL {
		@Override
		public TypeEraser getTypeEraser() {
			return GlobalTypeEraser.getInstance();
		}
	};

	/**
	 * Returns the {@link TypeEraser} implementation corresponding to a given
	 * type erasure mode.
	 * 
	 * @return the concrete type eraser
	 */
	public abstract TypeEraser getTypeEraser();

}