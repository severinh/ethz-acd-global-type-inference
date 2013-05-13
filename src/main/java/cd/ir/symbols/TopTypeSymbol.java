package cd.ir.symbols;

public final class TopTypeSymbol extends TypeSymbol {
	public TopTypeSymbol(String name) {
		super(name);
	}

	@Override
	public boolean isDeclarableType() {
		return false;
	}
}