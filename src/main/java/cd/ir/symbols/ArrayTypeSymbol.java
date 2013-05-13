package cd.ir.symbols;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class ArrayTypeSymbol extends TypeSymbol {

	public final TypeSymbol elementType;

	public ArrayTypeSymbol(TypeSymbol elementType) {
		super(makeNameFromElementType(elementType));
		checkNotNull(elementType);
		checkArgument(elementType.isDeclarableType());
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