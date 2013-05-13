package cd.ir.symbols;

/**
 * Represents the null type, which is a subtype of all reference types.
 */
public final class NullTypeSymbol extends TypeSymbol {

	/**
	 * Allow static access to the special type names from the parser.
	 * 
	 * The underscore prefix ensures that there will be no name clash with any
	 * type in user programs. Also, underscores are safe to use in assembly
	 * mnemonics, in contrast to "<" and ">".
	 */
	public static final String NAME = "_null";

	public static final NullTypeSymbol INSTANCE = new NullTypeSymbol();

	private NullTypeSymbol() {
		super(NAME);
	}

	@Override
	public boolean isReferenceType() {
		return true;
	}

	@Override
	public boolean isDeclarableType() {
		return false;
	}

}
