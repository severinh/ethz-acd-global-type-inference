package cd.ir.symbols;

public class ArrayTypeSymbol extends TypeSymbol {

	public final TypeSymbol elementType;

	public ArrayTypeSymbol(TypeSymbol elementType) {
		super(makeNameFromElementType(elementType));
		this.elementType = elementType;
	}

	@Override
	public boolean isReferenceType() {
		return true;
	}

	/**
	 * Returns the name of the array type corresponding to a given element type.
	 * 
	 * @param elementType
	 *            the type of the array elements
	 * @return the array type name
	 */
	public static String makeNameFromElementType(TypeSymbol elementType) {
		return elementType.name + "[]";
	}

}