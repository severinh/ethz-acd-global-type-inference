package cd.test;

import java.io.File;
import java.util.Collection;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import cd.CompilerOptions;
import cd.TypeErasureMode;
import cd.TypeInferenceMode;

/**
 * Performs all end-to-end tests with local type erasure and lightweight local
 * type inference.
 */
@RunWith(Parameterized.class)
public class LocalTypeInferenceLightweightTest extends TestSamplePrograms {

	@Parameters(name = "{index}: {0}")
	public static Collection<Object[]> getParameters() {
		CompilerOptions options = new CompilerOptions(TypeErasureMode.LOCAL,
				TypeInferenceMode.LOCAL_LIGHTWEIGHT);
		return buildParameters(options);
	}

	public LocalTypeInferenceLightweightTest(String testName, File file,
			CompilerOptions options) {
		super(testName, file, options);
	}

}
