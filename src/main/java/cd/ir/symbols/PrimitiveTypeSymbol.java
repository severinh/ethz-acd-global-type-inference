package cd.ir.symbols;

public class PrimitiveTypeSymbol extends TypeSymbol {

	public PrimitiveTypeSymbol(String name) {
		super(name);
	}

	@Override
	public boolean isReferenceType() {
		return false;
	}

}