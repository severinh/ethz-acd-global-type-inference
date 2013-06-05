package cd;

public class CompilerOptions {

	private final TypeErasureMode typeErasureMode;
	private final TypeInferenceMode typeInferenceMode;
	private boolean enableDevirtualizationOptimization;

	public CompilerOptions() {
		this(TypeErasureMode.NONE, TypeInferenceMode.GLOBAL);
		setEnableDevirtualizationOptimization(false);
	}

	public CompilerOptions(TypeErasureMode erasure, TypeInferenceMode inference) {
		this.typeErasureMode = erasure;
		this.typeInferenceMode = inference;
	}

	public TypeErasureMode getTypeErasureMode() {
		return typeErasureMode;
	}

	public TypeInferenceMode getTypeInferenceMode() {
		return typeInferenceMode;
	}

	public boolean isEnableDevirtualizationOptimization() {
		return enableDevirtualizationOptimization;
	}

	public void setEnableDevirtualizationOptimization(
			boolean enableDevirtualizationOptimization) {
		this.enableDevirtualizationOptimization = enableDevirtualizationOptimization;
	}

}
