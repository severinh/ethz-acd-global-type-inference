package cd.test;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import cd.CompilationContext;
import cd.Main;

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

	@Parameters
	public static Collection<Object[]> testFiles() {
		List<Object[]> result = new ArrayList<>();
		IOFileFilter fileFilter = new SuffixFileFilter(".javali");
		if (JUST_FILE != null) {
			result.add(new Object[] { JUST_FILE });
		} else if (TEST_DIR != null) {
			for (File file : FileUtils.listFiles(TEST_DIR, fileFilter,
					TrueFileFilter.INSTANCE)) {
				result.add(new Object[] { file });
			}
		} else {
			for (File file : FileUtils.listFiles(new File("."), fileFilter,
					TrueFileFilter.INSTANCE)) {
				result.add(new Object[] { file });
			}
		}
		return result;
	}

	/**
	 * @param file
	 *            The javali file to test.
	 */
	public TestSamplePrograms(File file) {
		this.compilation = new CompilationContext(file);
		this.testReferenceData = new TestReferenceData(file);
		this.infile = new File(file.getPath() + ".in");
		this.errfile = new File(String.format("%s.err", file.getPath()));
		this.main = new Main();
	}

}
