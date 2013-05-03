package cd.test;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class TestSamplePrograms extends AbstractTestSamplePrograms {

	/**
	 * If you want to run the test on just one file, then initialize this
	 * variable like:
	 * {@code JUST_FILE = new File("javali_tests/A2/Inheritance.javali")}.
	 */
	// public static final File JUST_FILE = new
	// File("javali_tests/test.javali");
	public static final File JUST_FILE = null;

	/**
	 * Directory in which to search for test files. If null, then the default is
	 * the current directory (to include all files). To run only tests in a
	 * particular directory, use something like:
	 * {@code TEST_DIR = new File("javali_tests/A2/")}.
	 */
	public static final File TEST_DIR = new File(TestSamplePrograms.class
			.getResource("/").getPath());

	@Parameters(name="{index}: {0}")
	public static Collection<Object[]> testFiles() {
		List<Object[]> result = new ArrayList<>();
		IOFileFilter fileFilter = new SuffixFileFilter(".javali");
		IOFileFilter dirFilter = new NotFileFilter(new NameFileFilter("newsyntax"));
		if (JUST_FILE != null) {
			result.add(new Object[] { JUST_FILE.getName(), JUST_FILE });
		} else if (TEST_DIR != null) {
			for (File file : FileUtils.listFiles(TEST_DIR, fileFilter,
					dirFilter)) {
				result.add(new Object[] { file.getName(),  file });
			}
		} else {
			for (File file : FileUtils.listFiles(new File("."), fileFilter,
					dirFilter)) {
				result.add(new Object[] { file.getName(), file });
			}
		}
		return result;
	}

	/**
	 * The constructor called by the parametric Junit test runner
	 * @param testName
	 * 			  Name of the test. Can be ignored, it is used to display 
	 * 			  the name in Junit output/eclipse
	 * @param file
	 *            The javali file to test.
	 */
	public TestSamplePrograms(String testName, File file) {
		super(file);
	}

}
