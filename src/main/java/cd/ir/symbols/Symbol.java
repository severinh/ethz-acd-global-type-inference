package cd.ir.symbols;

public abstract class Symbol {

	public final String name;

	protected Symbol(String name) {
		this.name = name;
	}

	/**
	 * Returns the name of the symbol.
	 */
	public String getName() {
		return name;
	}

}
