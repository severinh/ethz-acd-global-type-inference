package cd.test;

import java.io.File;
import java.util.Collection;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import cd.CompilerOptions;
import cd.test.reference.ReferenceData;
import cd.test.reference.ReferenceDataFactory;

/**
 * Performs all end-to-end tests with neither type erasure nor type inference.
 */
@RunWith(Parameterized.class)
public class VanillaTest extends TestSamplePrograms {

	@Parameters(name = "{index}: {0}")
	public static Collection<Object[]> getParameters() {
		CompilerOptions options = new CompilerOptions();
		return buildParameters(options, ReferenceDataFactory.makeRemote());
	}

	public VanillaTest(String testName, File file, CompilerOptions options,
			ReferenceData referenceData) {
		super(testName, file, options, referenceData);
	}

}
