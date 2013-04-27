package cd.ir.symbols;

import cd.codegen.AstCodeGenerator;

public class VariableSymbol extends Symbol {

	public static enum Kind {
		PARAM, LOCAL, FIELD
	}

	private TypeSymbol type;

	private final Kind kind;
	private final int version;

	/**
	 * Meaning depends on the kind of variable, but generally refers to the
	 * offset in bytes from some base ptr to where the variable is found.
	 * <ul>
	 * <li>{@code PARAM}, {@code LOCAL}: Offset from BP
	 * <li>{@code FIELD}: Offset from object
	 * <li>{@code CONSTANT}: N/A
	 * </ul>
	 * Computed in {@link AstCodeGenerator}.
	 */
	public int offset;

	public VariableSymbol(VariableSymbol v0sym, int version) {
		super(v0sym.name + "_" + version);

		this.type = v0sym.getType();
		this.kind = v0sym.getKind();
		this.offset = v0sym.offset;
		this.version = version;
	}

	public VariableSymbol(String name, TypeSymbol type) {
		this(name, type, Kind.PARAM);
	}

	public VariableSymbol(String name, TypeSymbol type, VariableSymbol.Kind kind) {
		super(name);

		assert (type != null);
		assert (kind != null);
		this.type = type;
		this.kind = kind;
		this.version = 0;
		this.offset = -1;
	}

	public TypeSymbol getType() {
		return type;
	}

	/**
	 * Sets the type of the variable symbol.
	 * 
	 * It must be modifiable in order to implement type inference. However, it
	 * must not be modified anymore once type checking has taken place.
	 * 
	 * @todo Maybe prevent changes after type checking by "freezing" the
	 *       variable symbol
	 */
	public void setType(TypeSymbol type) {
		assert (type != null);
		this.type = type;
	}

	public int getVersion() {
		return version;
	}

	public Kind getKind() {
		return kind;
	}

	@Override
	public String toString() {
		return name;
	}

}