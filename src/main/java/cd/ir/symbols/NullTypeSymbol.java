package cd.ir.symbols;

/**
 * Represents the null type, which is a subtype of all reference types.
 */
public final class NullTypeSymbol extends TypeSymbol {

	public static final NullTypeSymbol INSTANCE = new NullTypeSymbol();

	private NullTypeSymbol() {
		// The underscore prefix ensures that there will be no name clash with
		// any type in user programs. Also, underscores are safe to use in
		// assembly mnemonics, in contrast to "<" and ">".
		super("_null");
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
