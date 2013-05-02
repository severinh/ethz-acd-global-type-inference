package cd.semantic;

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

		A = new ClassSymbol("A", typeSymbols.getObjectType());
		B = new ClassSymbol("B", A);
		C = new ClassSymbol("C", B);
		D = new ClassSymbol("D", B);
		E = new ClassSymbol("E", typeSymbols.getObjectType());

		typeSymbols.add(A);
		typeSymbols.add(B);
		typeSymbols.add(C);
		typeSymbols.add(D);
		typeSymbols.add(E);

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
		TypeSymbol nullType = typeSymbols.getNullType();
		TypeSymbol intType = typeSymbols.getIntType();
		TypeSymbol floatType = typeSymbols.getFloatType();
		TypeSymbol booleanType = typeSymbols.getBooleanType();

		for (TypeSymbol type : typeSymbols.allSymbols()) {
			assertLCA(type, type, type);
			assertLCA(topType, topType, type);
			assertLCA(type, bottomType, type);

			if (type.isReferenceType()) {
				assertLCA(type, nullType, type);
			}
		}

		assertLCA(topType, intType, booleanType);
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
