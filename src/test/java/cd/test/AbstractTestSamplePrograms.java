package cd.test;

import java.io.File;
import java.io.IOException;

import javax.annotation.Nonnull;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;

import cd.CompilationContext;
import cd.Compiler;
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

	private final Compiler compiler;

	private final ReferenceData referenceData;
	private final File inputFile;
	private final TestConfig testConfig;

	public AbstractTestSamplePrograms(@Nonnull File sourceFile,
			@Nonnull CompilerOptions options, ReferenceData referenceData) {
		CompilationContext context = new CompilationContext(sourceFile, options);
		this.compiler = Compiler.forContext(context);
		this.referenceData = referenceData;
		this.inputFile = new File(context.getSourceFile().getPath() + ".in");
		this.testConfig = new TestConfig(); // Could be passed as parameter
	}

	protected Compiler getCompiler() {
		return compiler;
	}

	@After
	public void tearDown() {
		getCompiler().getContext().deleteIntermediateFiles();
	}

	@Test
	public void test() throws Throwable {
		LOG.debug("Testing " + getCompiler().getContext().getSourceFile());

		Optional<ParseFailure> parseFailure = Optional.absent();
		Optional<SemanticFailure> semanticFailure = Optional.absent();

		try {
			compiler.compile();
		} catch (ParseFailure failure) {
			parseFailure = Optional.of(failure);
		} catch (SemanticFailure failure) {
			semanticFailure = Optional.of(failure);
		}

		Optional<ParseFailure> parseFailureRef = referenceData
				.getParseFailure();

		if (parseFailureRef.isPresent() || parseFailure.isPresent()) {
			assertEqualsWithException(parseFailure,
					parseFailureRef.isPresent(), parseFailure.isPresent());
		} else {
			// Only fetch the semantic failure reference data and run the
			// remaining tests if parsing was successful
			Optional<SemanticFailure> semanticFailureRef = referenceData
					.getSemanticFailure();
			Optional<Cause> semanticFailureCauseRef = getSemanticFailureCause(semanticFailureRef);
			Optional<Cause> semanticFailureCause = getSemanticFailureCause(semanticFailure);

			assertEqualsWithException(semanticFailure, semanticFailureCauseRef,
					semanticFailureCause);

			if (!semanticFailure.isPresent()) {
				// Only run the remaining tests if the semantic check was
				// successful
				testCodeGenerator();
			}
		}
	}

	/**
	 * Assert that two values are equal and optionally use an exception as the
	 * message if the assertion fails.
	 * 
	 * @param exception
	 *            the optional exception. Note that it may be absent even though
	 *            the two values are unequal and it may be present even though
	 *            the values are equal. In other words, its presence is
	 *            independent of the values being equal or unequal
	 * @param expected
	 *            expected value
	 * @param actual
	 *            actual value
	 */
	private static void assertEqualsWithException(
			Optional<? extends Exception> exception, Object expected,
			Object actual) {
		String message = "";
		if (exception.isPresent()) {
			message = ExceptionUtils.getStackTrace(exception.get());
		}
		Assert.assertEquals(message, expected, actual);
	}

	private static Optional<Cause> getSemanticFailureCause(
			Optional<SemanticFailure> semanticFailure) {
		if (semanticFailure.isPresent()) {
			return Optional.of(semanticFailure.get().cause);
		} else {
			return Optional.absent();
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

		if (!execRef.contains("NONDETERMINISTIC")) {
			Assert.assertEquals(execRef, execOut);
		}
	}

	private String getInput() throws IOException {
		String input = "";
		if (inputFile.exists()) {
			input = FileUtils.readFileToString(inputFile);
		}
		return input;
	}

}
