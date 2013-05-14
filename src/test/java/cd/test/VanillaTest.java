package cd.test;

import java.io.File;
import java.util.Collection;

import javax.annotation.Nonnull;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import cd.CompilerOptions;
import cd.test.fileprovider.RecursiveTestFileProvider;
import cd.test.fileprovider.TestFileProvider;
import cd.test.reference.ReferenceData;
import cd.test.reference.RemoteReferenceDataFactory;

/**
 * Performs all end-to-end tests with neither type erasure nor type inference.
 */
@RunWith(Parameterized.class)
public class VanillaTest extends TestSamplePrograms {

	@Parameters(name = "{index}: {0}")
	public static Collection<Object[]> getParameters() {
		TestFileProvider testFileProvider = RecursiveTestFileProvider
				.withExcludedDir(TEST_FOLDER, "newsyntax");
		CompilerOptions options = new CompilerOptions();
		return buildParameters(testFileProvider, options,
				new RemoteReferenceDataFactory());
	}

	public VanillaTest(String testName, @Nonnull File file,
			@Nonnull CompilerOptions options, ReferenceData referenceData) {
		super(testName, file, options, referenceData);
	}

}
