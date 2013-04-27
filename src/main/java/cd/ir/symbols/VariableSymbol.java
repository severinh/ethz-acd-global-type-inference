package cd.ir.symbols;

import cd.codegen.AstCodeGenerator;

public class VariableSymbol extends Symbol {

	public static enum Kind {
		PARAM, LOCAL, FIELD
	}

	/**
	 * The type of the variable symbol must be modifiable in order to implement
	 * type inference. However, it must not be modified anymore once type
	 * checking has taken place.
	 * 
	 * @todo Use a getter and setter for the type symbol
	 * @todo Maybe prevent changes after type checking by "freezing" the
	 *       variable symbol
	 */
	public TypeSymbol type;

	public final Kind kind;
	public final int version;

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
		this.type = v0sym.type;
		this.kind = v0sym.kind;
		this.offset = v0sym.offset;
		this.version = version;
	}

	public VariableSymbol(String name, TypeSymbol type) {
		this(name, type, Kind.PARAM);
	}

	public VariableSymbol(String name, TypeSymbol type, VariableSymbol.Kind kind) {
		super(name);
		this.type = type;
		this.kind = kind;
		this.version = 0;
		this.offset = -1;
	}

	@Override
	public String toString() {
		return name;
	}

}