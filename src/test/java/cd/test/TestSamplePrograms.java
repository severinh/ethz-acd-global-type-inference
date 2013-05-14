package cd.test;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;

import cd.CompilerOptions;
import cd.test.fileprovider.TestFileProvider;
import cd.test.reference.ReferenceData;
import cd.test.reference.ReferenceDataFactory;

public abstract class TestSamplePrograms extends AbstractTestSamplePrograms {

	protected static final File TEST_FOLDER;

	static {
		String folderPath = TestSamplePrograms.class.getResource("/").getPath();
		TEST_FOLDER = new File(folderPath);
	}

	protected static Collection<Object[]> buildParameters(
			TestFileProvider testFileProvider, CompilerOptions options,
			ReferenceDataFactory referenceDataFactory) {
		List<Object[]> result = new ArrayList<>();
		for (File file : testFileProvider.getTestFiles()) {
			result.add(new Object[] { file.getName(), file, options,
					referenceDataFactory.of(file) });
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
	public TestSamplePrograms(String testName, @Nonnull File file,
			@Nonnull CompilerOptions options, ReferenceData referenceData) {
		super(file, options, referenceData);
	}

}
