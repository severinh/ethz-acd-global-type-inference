package cd.semantic;

import java.util.ArrayList;
import java.util.List;

import cd.ir.symbols.ClassSymbol;
import cd.ir.symbols.PrimitiveTypeSymbol;
import cd.ir.symbols.TypeSymbol;

/**
 * Table that holds all type symbols of the program.
 * 
 * All type symbols in this table can be used from anywhere in the program.
 */
public class TypeSymbolTable extends SymbolTable<TypeSymbol> {

	/**
	 * Symbols for the built-in primitive types.
	 */
	private final PrimitiveTypeSymbol intType;
	private final PrimitiveTypeSymbol floatType;
	private final PrimitiveTypeSymbol voidType;
	private final PrimitiveTypeSymbol booleanType;

	/**
	 * Symbols for the built-in Object and null types.
	 */
	private final ClassSymbol objectType;
	private final ClassSymbol nullType;

	/**
	 * Symbol for the built-in top and bottom type.
	 */
	private final TypeSymbol topType;
	private final TypeSymbol bottomType;

	public TypeSymbolTable() {
		super(null);

		intType = new PrimitiveTypeSymbol("int");
		floatType = new PrimitiveTypeSymbol("float");
		booleanType = new PrimitiveTypeSymbol("boolean");
		voidType = new PrimitiveTypeSymbol("void");
		objectType = new ClassSymbol("Object");
		nullType = new ClassSymbol("<null>");
		topType = new TypeSymbol("<top>");
		bottomType = new TypeSymbol("<bottom>");

		add(intType);
		add(booleanType);
		add(floatType);
		add(voidType);
		add(objectType);

		// The following type symbols were not part of the symbol table so far
		// TODO: Is it a problem to add them now?
		// add(nullType);
		// add(topType);
		// add(bottomType);
	}

	public PrimitiveTypeSymbol getIntType() {
		return intType;
	}

	public PrimitiveTypeSymbol getFloatType() {
		return floatType;
	}

	public PrimitiveTypeSymbol getVoidType() {
		return voidType;
	}

	public PrimitiveTypeSymbol getBooleanType() {
		return booleanType;
	}

	public ClassSymbol getObjectType() {
		return objectType;
	}

	public ClassSymbol getNullType() {
		return nullType;
	}

	public TypeSymbol getTopType() {
		return topType;
	}

	public TypeSymbol getBottomType() {
		return bottomType;
	}

	/**
	 * Returns the list of all class symbols in this symbol table.
	 * 
	 * It also includes built-in class symbols like <code>Object</code> and
	 * <code>null</code>.
	 * 
	 * @return the class symbols
	 */
	public List<ClassSymbol> getClassSymbols() {
		List<ClassSymbol> classSymbols = new ArrayList<>();
		for (TypeSymbol typeSymbol : localSymbols()) {
			if (typeSymbol instanceof ClassSymbol) {
				classSymbols.add((ClassSymbol) typeSymbol);
			}
		}
		return classSymbols;
	}

}
