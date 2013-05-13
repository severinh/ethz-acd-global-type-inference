package cd.ir.symbols;

/**
 * Represents the bottom type, which is a subtype of all types.
 */
public final class BottomTypeSymbol extends TypeSymbol {

	/**
	 * Allow static access to the special type names from the parser.
	 * 
	 * The underscore prefix ensures that there will be no name clash with any
	 * type in user programs. Also, underscores are safe to use in assembly
	 * mnemonics, in contrast to "<" and ">".
	 */
	public static final String NAME = "_bottom";

	public static final BottomTypeSymbol INSTANCE = new BottomTypeSymbol();

	private BottomTypeSymbol() {
		super(NAME);
	}

	@Override
	public boolean isDeclarableType() {
		return false;
	}

}