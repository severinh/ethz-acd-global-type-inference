package cd.test;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import cd.CompilerOptions;
import cd.TypeErasureMode;
import cd.TypeInferenceMode;

@RunWith(Parameterized.class)
public class TestSamplePrograms extends AbstractTestSamplePrograms {

	private static final TestFileProvider TEST_FILE_PROVIDER;

	static {
		// Run the tests on just one file
		// String filePath = "test.javali";
		// TEST_FILE_PROVIDER = new SingleTestFileProvider(new File(filePath));

		// Run the tests on all files in a directory (search recursively)
		String folderPath = TestSamplePrograms.class.getResource("/").getPath();
		TEST_FILE_PROVIDER = RecursiveTestFileProvider.withExcludedDir(
				new File(folderPath), "global");
	}

	@Parameters(name = "{index}: {0}")
	public static Collection<Object[]> testFiles() {
		List<Object[]> result = new ArrayList<>();
		CompilerOptions options = new CompilerOptions(TypeErasureMode.LOCAL,
				TypeInferenceMode.LOCAL);
		for (File file : TEST_FILE_PROVIDER.getTestFiles()) {
			result.add(new Object[] { file.getName(), file, options });
		}
		return result;
	}

	/**
	 * The constructor called by the parametric JUnit test runner
	 * 
	 * @param testName
	 *            Name of the test. Can be ignored, it is used to display the
	 *            name in JUnit output/eclipse
	 * @param file
	 *            The javali file to test.
	 */
	public TestSamplePrograms(String testName, File file,
			CompilerOptions options) {
		super(file, options);
	}

}
