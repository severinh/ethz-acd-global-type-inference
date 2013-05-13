package cd.ir.symbols;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkArgument;

public abstract class Symbol {

	public final String name;

	protected Symbol(String name) {
		checkNotNull(name);
		checkArgument(!name.isEmpty());

		this.name = name;
	}

	/**
	 * Returns the name of the symbol.
	 */
	public String getName() {
		return name;
	}

}
