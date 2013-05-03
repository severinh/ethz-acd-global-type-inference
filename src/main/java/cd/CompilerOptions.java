package cd;

public class CompilerOptions {
	private final TypeErasureMode typeErasure;
	private final TypeInferenceMode typeInference;
	
	public CompilerOptions() {
		this(TypeErasureMode.NONE, TypeInferenceMode.NONE);
	}

	public CompilerOptions(TypeErasureMode erasure, TypeInferenceMode inference) {
		this.typeErasure = erasure;
		this.typeInference = inference;
	}	
	
	public TypeErasureMode getTypeErasure() {
		return typeErasure;
	}

	public TypeInferenceMode getTypeInference() {
		return typeInference;
	}
	
	public static enum TypeErasureMode {
		NONE,
		LOCAL,
		GLOBAL
	}
	
	public static enum TypeInferenceMode {
		NONE,
		LOCAL
	}
}
