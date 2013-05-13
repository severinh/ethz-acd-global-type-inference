package cd.ir.symbols;

/**
 * Represents the top type, which is a supertype of all types.
 */
public final class TopTypeSymbol extends TypeSymbol {

	/**
	 * Allow static access to the special type names from the parser.
	 * 
	 * The underscore prefix ensures that there will be no name clash with any
	 * type in user programs. Also, underscores are safe to use in assembly
	 * mnemonics, in contrast to "<" and ">".
	 */
	public static final String NAME = "_top";

	public static final TopTypeSymbol INSTANCE = new TopTypeSymbol();

	public TopTypeSymbol() {
		super(NAME);
	}

	@Override
	public boolean isDeclarableType() {
		return false;
	}

}