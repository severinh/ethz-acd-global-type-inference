package cd.ir.symbols;

/**
 * Represents the top type, which is a supertype of all types.
 */
public final class TopTypeSymbol extends TypeSymbol {

	public static final TopTypeSymbol INSTANCE = new TopTypeSymbol();

	public TopTypeSymbol() {
		// The underscore prefix ensures that there will be no name clash with
		// any type in user programs. Also, underscores are safe to use in
		// assembly mnemonics, in contrast to "<" and ">".
		super("_top");
	}

	@Override
	public boolean isDeclarableType() {
		return false;
	}

}