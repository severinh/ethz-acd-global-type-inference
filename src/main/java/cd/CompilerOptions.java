package cd;

public class CompilerOptions {

	private final TypeErasureMode typeErasureMode;
	private final TypeInferenceMode typeInferenceMode;

	public CompilerOptions() {
		this(TypeErasureMode.NONE, TypeInferenceMode.NONE);
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

}
