package cd.ir.symbols;

/**
 * Represents the null type, which is a subtype of all reference types.
 */
public class NullTypeSymbol extends TypeSymbol {

	public NullTypeSymbol(String name) {
		super(name);
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
