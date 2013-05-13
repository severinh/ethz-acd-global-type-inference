package cd.ir.symbols;

public final class BottomTypeSymbol extends TypeSymbol {
	public BottomTypeSymbol(String name) {
		super(name);
	}

	@Override
	public boolean isDeclarableType() {
		return false;
	}
}