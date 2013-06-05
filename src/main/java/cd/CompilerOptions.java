package cd;

public class CompilerOptions {

	private TypeErasureMode typeErasureMode;
	private TypeInferenceMode typeInferenceMode;
	private boolean devirtualizing;
	private boolean debugging;

	public CompilerOptions() {
		this(TypeErasureMode.NONE, TypeInferenceMode.NONE);
	}

	public CompilerOptions(TypeErasureMode erasure, TypeInferenceMode inference) {
		this.typeErasureMode = erasure;
		this.typeInferenceMode = inference;
		this.devirtualizing = false;
		this.debugging = false;
	}

	public TypeErasureMode getTypeErasureMode() {
		return typeErasureMode;
	}

	public void setTypeErasureMode(TypeErasureMode typeErasureMode) {
		this.typeErasureMode = typeErasureMode;
	}

	public TypeInferenceMode getTypeInferenceMode() {
		return typeInferenceMode;
	}

	public void setTypeInferenceMode(TypeInferenceMode typeInferenceMode) {
		this.typeInferenceMode = typeInferenceMode;
	}

	public boolean isDevirtualizing() {
		return devirtualizing;
	}

	public void setDevirtualizing(boolean devirtualizing) {
		this.devirtualizing = devirtualizing;
	}

	public boolean isDebugging() {
		return debugging;
	}

	public void setDebugging(boolean debug) {
		this.debugging = debug;
	}

}
