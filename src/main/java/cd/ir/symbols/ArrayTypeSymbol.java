package cd.ir.symbols;

public class ArrayTypeSymbol extends TypeSymbol {

	public final TypeSymbol elementType;

	public ArrayTypeSymbol(TypeSymbol elementType) {
		super(elementType.name + "[]");
		this.elementType = elementType;
	}

	@Override
	public boolean isReferenceType() {
		return true;
	}

}