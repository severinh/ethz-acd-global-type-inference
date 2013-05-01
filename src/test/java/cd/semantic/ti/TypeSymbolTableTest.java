package cd.semantic.ti;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import cd.ir.symbols.ClassSymbol;
import cd.ir.symbols.TypeSymbol;
import cd.semantic.TypeSymbolTable;

/**
 * Tests {@link TypeSymbolTable}.
 */
public class TypeSymbolTableTest {

	private TypeSymbolTable typeSymbols;
	private ClassSymbol A, B, C, D, E;

	@Before
	public void setUp() {
		typeSymbols = new TypeSymbolTable();

		A = new ClassSymbol("A");
		B = new ClassSymbol("B");
		C = new ClassSymbol("C");
		D = new ClassSymbol("D");
		E = new ClassSymbol("E");

		A.superClass = typeSymbols.getObjectType();
		B.superClass = A;
		C.superClass = B;
		D.superClass = B;
		E.superClass = typeSymbols.getObjectType();

		// Object
		// |-- A
		// |...|-- B
		// |.......|-- C
		// |.......|-- D
		// |-- E
	}

	@Test
	public void testLCA() {
		TypeSymbol topType = typeSymbols.getTopType();
		TypeSymbol bottomType = typeSymbols.getBottomType();
		TypeSymbol objectType = typeSymbols.getObjectType();
		TypeSymbol intType = typeSymbols.getIntType();
		TypeSymbol floatType = typeSymbols.getFloatType();

		for (TypeSymbol type : typeSymbols.allSymbols()) {
			assertLCA(type, type, type);
			assertLCA(type, bottomType, type);
		}

		assertLCA(topType, intType, floatType);
		assertLCA(topType, intType, topType);
		assertLCA(topType, intType, objectType);
		assertLCA(topType, objectType, topType);

		assertLCA(objectType, A, objectType);
		assertLCA(objectType, A, E);
		assertLCA(A, A, B);
		assertLCA(B, C, D);
	}

	private void assertLCA(TypeSymbol expectedLCA, TypeSymbol type,
			TypeSymbol other) {
		Assert.assertEquals(expectedLCA, typeSymbols.getLCA(type, other));
		Assert.assertEquals(expectedLCA, typeSymbols.getLCA(other, type));
	}

}
