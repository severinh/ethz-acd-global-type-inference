package cd.test;

import java.io.File;
import java.util.Collection;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import cd.CompilerOptions;
import cd.TypeErasureMode;
import cd.TypeInferenceMode;
import cd.test.fileprovider.RecursiveTestFileProvider;
import cd.test.fileprovider.TestFileProvider;
import cd.test.reference.ReferenceData;
import cd.test.reference.ReferenceDataFactory;

/**
 * Performs all end-to-end tests with local type erasure and constraint-based
 * local type inference.
 */
@RunWith(Parameterized.class)
public class LocalTypeInferenceWithConstraintsTest extends TestSamplePrograms {

	@Parameters(name = "{index}: {0}")
	public static Collection<Object[]> getParameters() {
		TestFileProvider testFileProvider = RecursiveTestFileProvider
				.withExcludedDir(TEST_FOLDER, "global");
		CompilerOptions options = new CompilerOptions(TypeErasureMode.LOCAL,
				TypeInferenceMode.LOCAL_CONSTRAINTS);
		return buildParameters(testFileProvider, options,
				ReferenceDataFactory.makeLocalOverridingRemote());
	}

	public LocalTypeInferenceWithConstraintsTest(String testName, File file,
			CompilerOptions options, ReferenceData referenceData) {
		super(testName, file, options, referenceData);
	}

}
