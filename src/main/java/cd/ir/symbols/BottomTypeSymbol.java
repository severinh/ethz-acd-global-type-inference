package cd.ir.symbols;

/**
 * Represents the bottom type, which is a subtype of all types.
 */
public final class BottomTypeSymbol extends TypeSymbol {

	public static final BottomTypeSymbol INSTANCE = new BottomTypeSymbol();

	private BottomTypeSymbol() {
		// The underscore prefix ensures that there will be no name clash with
		// any type in user programs. Also, underscores are safe to use in
		// assembly mnemonics, in contrast to "<" and ">".
		super("_bottom");
	}

	@Override
	public boolean isDeclarableType() {
		return false;
	}

}