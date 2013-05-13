package cd.test;

import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;

import cd.CompilationContext;
import cd.CompilerToolchain;
import cd.CompilerOptions;
import cd.exceptions.ParseFailure;
import cd.exceptions.SemanticFailure;
import cd.exceptions.SemanticFailure.Cause;
import cd.test.reference.ReferenceData;
import cd.util.FileUtil;

abstract public class AbstractTestSamplePrograms {

	private static final Logger LOG = LoggerFactory
			.getLogger(AbstractTestSamplePrograms.class);

	// We let valgrind return a special exit code if it detect a problem.
	// Otherwise valgrind returns the exit code of the simulated program.
	private static final int VALGRIND_ERROR_CODE = 77;

	private final CompilerToolchain compiler;

	private final ReferenceData referenceData;
	private final File inputFile;
	private final TestConfig testConfig;

	public AbstractTestSamplePrograms(File sourceFile, CompilerOptions options) {
		CompilationContext context = new CompilationContext(sourceFile, options);
		this.compiler = CompilerToolchain.forContext(context);
		this.referenceData = ReferenceData.localOverridingRemote(sourceFile);
		this.inputFile = new File(context.getSourceFile().getPath() + ".in");
		this.testConfig = new TestConfig(); // Could be passed as parameter
	}

	protected CompilerToolchain getCompiler() {
		return compiler;
	}

	@After
	public void tearDown() {
		getCompiler().getContext().deleteIntermediateFiles();
	}

	@Test
	public void test() throws Throwable {
		LOG.debug("Testing " + getCompiler().getContext().getSourceFile());

		boolean isParseFailureRef = referenceData.isParseFailure();
		boolean isParseFailure = false;
		Optional<Cause> semanticFailureCause = Optional.absent();

		try {
			compiler.compile();
		} catch (ParseFailure parseFailure) {
			LOG.debug(ExceptionUtils.getStackTrace(parseFailure));
			isParseFailure = true;
		} catch (SemanticFailure semanticFailure) {
			LOG.debug(ExceptionUtils.getStackTrace(semanticFailure));
			semanticFailureCause = Optional.of(semanticFailure.cause);
		}

		if (isParseFailureRef || isParseFailure) {
			Assert.assertEquals(isParseFailureRef, isParseFailure);
		} else {
			// Only fetch the semantic failure reference data and run the
			// remaining tests if parsing was successful
			Optional<Cause> semanticFailureCauseRef = referenceData
					.getSemanticFailureCause();
			Assert.assertEquals(semanticFailureCauseRef, semanticFailureCause);

			if (!semanticFailureCause.isPresent()) {
				// Only run the remaining tests if the semantic check was
				// successful
				testCodeGenerator();
			}
		}
	}

	/**
	 * Run the code generator, assemble the resulting .s file, and (if the
	 * output is well-defined) compare against the expected output.
	 */
	private void testCodeGenerator() throws IOException {
		// Determine the input and expected output
		String input = getInput();
		String binaryFilePath = getCompiler().getContext().getBinaryFile()
				.getAbsolutePath();
		String execRef = referenceData.getExecutionReference(input);

		// Execute the binary file, providing input if relevant, and
		// capturing the output. Check the error code so see if the
		// code signaled dynamic errors.
		String execOut = FileUtil.runCommand(new File("."),
				new String[] { binaryFilePath }, new String[] {}, input, true);

		if (testConfig.isValgrindEnabled()) {
			String[] valgrindCommand = new String[] { "valgrind",
					"--error-exitcode=" + VALGRIND_ERROR_CODE, binaryFilePath };
			String valgrindOut = FileUtil.runCommand(new File("."),
					valgrindCommand, new String[] {}, input, true);
			Assert.assertFalse(valgrindOut.contains("Error: "
					+ VALGRIND_ERROR_CODE));
		}

		Assert.assertEquals(execRef, execOut);
	}

	private String getInput() throws IOException {
		String input = "";
		if (inputFile.exists()) {
			input = FileUtils.readFileToString(inputFile);
		}
		return input;
	}

}
