package cd.ir.symbols;


public final class BottomTypeSymbol extends TypeSymbol {
	public BottomTypeSymbol(String name) {
		super(name);
	}

	public boolean isDeclarableType() {
		return false;
	}
}