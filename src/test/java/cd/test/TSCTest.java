package cd.test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cd.CompilationContext;
import cd.CompilerOptions;
import cd.CompilerToolchain;
import cd.test.fileprovider.RecursiveTestFileProvider;
import cd.test.fileprovider.TestFileProvider;
import cd.util.FileUtil;

@RunWith(Parameterized.class)
public class TSCTest {
	
	private static final Logger LOG = LoggerFactory
			.getLogger(TSCTest.class);

	protected static final File TEST_FOLDER;
	private static final int VALGRIND_ERROR_CODE = 77;

	static {
		String folderPath = TestSamplePrograms.class.getResource(
				"/ACD_Project/newsyntax/tsc/").getPath();
		TEST_FOLDER = new File(folderPath);
	}

	@Parameters(name = "{index}: {0}")
	public static Collection<Object[]> getParameters() {
		TestFileProvider testFileProvider = new RecursiveTestFileProvider(
				TEST_FOLDER);
		CompilerOptions options = new CompilerOptions();
		List<Object[]> result = new ArrayList<>();
		for (File file : testFileProvider.getTestFiles()) {
			result.add(new Object[] { file.getName(), file, options });
		}
		return result;
	}

	private CompilationContext context;
	private CompilerToolchain compiler;

	public TSCTest(String testName, @Nonnull File sourceFile,
			@Nonnull CompilerOptions options) {
		context = new CompilationContext(sourceFile, options);
		compiler = CompilerToolchain.forContext(context);
	}

	@Test
	public void test() {
		try {
			compiler.compile();

			String binaryFilePath = compiler.getContext().getBinaryFile()
					.getAbsolutePath();

			LOG.info("Running compiled program");
			String execOut = FileUtil.runCommand(new File("."),
					new String[] { binaryFilePath }, new String[] {}, null,
					true);
			Assert.assertFalse(execOut.contains("Error"));
			
			LOG.info("Output of " + context.getSourceFile().getName() + " was: " + execOut);

			TestConfig tc= new TestConfig();
			if (tc.isValgrindEnabled()) {
				String[] valgrindCommand = new String[] { "valgrind",
						"--error-exitcode=" + VALGRIND_ERROR_CODE, binaryFilePath };
				String valgrindOut = FileUtil.runCommand(new File("."),
						valgrindCommand, new String[] {}, null, true);
				Assert.assertFalse(valgrindOut.contains("Error: "
						+ VALGRIND_ERROR_CODE));
			}

		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}
