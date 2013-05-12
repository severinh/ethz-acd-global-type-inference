package cd.semantic.ti;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableSet;

import cd.ir.symbols.ClassSymbol;
import cd.ir.symbols.MethodSymbol;
import cd.ir.symbols.TypeSymbol;
import cd.ir.symbols.VariableSymbol;
import cd.semantic.TypeSymbolTable;

/**
 * Tests {@link MethodSymbolCache}.
 */
public class MethodSymbolCacheTest {

	private TypeSymbolTable types;
	private ClassSymbol xClass;
	private ClassSymbol yClass;
	private ClassSymbol zClass;
	private MethodSymbol xClassMMethod;
	private MethodSymbol yClassMMethod;
	private MethodSymbol zClassMMethod;
	private MethodSymbolCache cache;

	@Before
	public void setUp() {
		types = new TypeSymbolTable();

		TypeSymbol intType = types.getIntType();
		TypeSymbol floatType = types.getFloatType();

		xClass = new ClassSymbol("X", types.getObjectType());
		yClass = new ClassSymbol("Y", types.getObjectType());
		zClass = new ClassSymbol("Z", types.getObjectType());

		xClassMMethod = new MethodSymbol("m", xClass);
		xClassMMethod.addParameter(new VariableSymbol("i", intType));
		xClassMMethod.addParameter(new VariableSymbol("j", intType));
		xClass.addMethod(xClassMMethod);

		yClassMMethod = new MethodSymbol("m", yClass);
		yClassMMethod.addParameter(new VariableSymbol("i", floatType));
		yClassMMethod.addParameter(new VariableSymbol("j", floatType));
		yClass.addMethod(yClassMMethod);

		zClassMMethod = new MethodSymbol("m", zClass);
		zClassMMethod.addParameter(new VariableSymbol("o", intType));
		zClass.addMethod(zClassMMethod);

		types.add(xClass);
		types.add(yClass);
		types.add(zClass);

		cache = MethodSymbolCache.of(types);
	}

	@Test
	public void test() {
		Assert.assertTrue(cache.get("m", 0).isEmpty());
		Assert.assertEquals(ImmutableSet.of(zClassMMethod),
				ImmutableSet.copyOf(cache.get("m", 1)));
		Assert.assertEquals(ImmutableSet.of(xClassMMethod, yClassMMethod),
				ImmutableSet.copyOf(cache.get("m", 2)));
	}

}
