package cd.test;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cd.CompilationContext;
import cd.CompilerToolchain;
import cd.exceptions.ParseFailure;
import cd.exceptions.SemanticFailure;
import cd.exceptions.SemanticFailure.Cause;
import cd.util.FileUtil;

abstract public class AbstractTestSamplePrograms {

	private static final Logger LOG = LoggerFactory
			.getLogger(AbstractTestSamplePrograms.class);

	private static final boolean RUN_VALGRIND = false;

	// We let valgrind return a special exit code if it detect a problem.
	// Otherwise valgrind returns the exit code of the simulated program.
	private static final int VALGRIND_ERROR_CODE = 77;

	private final CompilerToolchain compiler;
	private final CompilationContext context;

	private final File infile;
	private final ParsedReferenceData referenceData;

	public AbstractTestSamplePrograms(File file) {
		this.context = new CompilationContext(file);
		this.compiler = CompilerToolchain.forContext(this.context);
		this.referenceData = new ParsedReferenceData(new CachedReferenceData(
				new RemoteReferenceData(file)));
		this.infile = new File(file.getPath() + ".in");
	}

	public void assertEquals(String phase, String exp, String act) {
		act = act.replace("\r\n", "\n"); // for windows machines
		if (!exp.equals(act)) {
			warnAboutDiff(phase, exp, act);
		}
	}

	/**
	 * Compare the output of two executions while ignoring small differences due
	 * to floating point errors. E.g. outputs "1.23456" and "1.23455" are OK
	 * even though they are slightly different.
	 */
	public void assertEqualOutput(String phase, String exp, String act) {
		act = act.replace("\r\n", "\n"); // for windows machines
		if (!exp.equals(act)) {
			String[] expLines = exp.split("\n");
			String[] actLines = act.split("\n");
			if (expLines.length != actLines.length)
				warnAboutDiff(phase, exp, act);
			for (int lineNb = 0; lineNb < expLines.length; lineNb++) {
				String expLine = expLines[lineNb];
				String actLine = actLines[lineNb];
				// assumption: all output w/ a dot is a floating point nb.
				if (expLine.contains(".") && actLine.contains(".")) {
					// allow rounding differences when comparing floating points
					// (known bug: this doesn't work if there are two floats on
					// a single output line)
					float expFloat = Float.valueOf(expLine);
					float actFloat = Float.valueOf(actLine);
					if (Math.abs(expFloat - actFloat) > 0.001)
						warnAboutDiff(phase, exp, act);
				} else {
					if (!expLine.equals(actLine))
						warnAboutDiff(phase, exp, act);
				}
			}
		}
	}

	private void warnAboutDiff(String phase, String exp, String act) {
		Assert.assertEquals(String.format("Phase %s for %s failed!", phase,
				context.getSourceFile().getPath()), exp, act);
	}

	@Test
	public void test() throws Throwable {
		LOG.debug("Testing " + context.getSourceFile());

		context.deleteIntermediateFiles();

		boolean isParserFailureRef = referenceData.isParserFailure();
		boolean isParserFailure = false;
		Cause semanticFailureCauseRef = null;
		Cause semanticFailureCause = null;

		try {
			compiler.compile();
		} catch (ParseFailure parseFailure) {
			LOG.debug(ExceptionUtils.getStackTrace(parseFailure));
			isParserFailure = true;
		} catch (SemanticFailure semanticFailure) {
			LOG.debug(ExceptionUtils.getStackTrace(semanticFailure));
			semanticFailureCause = semanticFailure.cause;
		}

		if (!isParserFailureRef && !isParserFailure) {
			// Only run the remaining tests if parsing was successful
			semanticFailureCauseRef = referenceData.getSemanticFailureCause();
			Assert.assertEquals(semanticFailureCauseRef, semanticFailureCause);

			if (semanticFailureCause == null) {
				// Only run the remaining tests if the semantic check was
				// successful
				testCodeGenerator();
				testOptimizer();
			}
		} else {
			Assert.assertEquals(isParserFailureRef, isParserFailure);
		}
	}

	private void testOptimizer() throws IOException {
		// Determine the input and expected operation counts.
		String inFile = (infile.exists() ? FileUtils.readFileToString(infile)
				: "");
		String optRef = referenceData.getOptimizationReference(inFile);

		// Invoke the interpreter. Don't bother to save the output: we already
		// verified that in testCodeGenerator().
		Interpreter interp = new Interpreter(context.getAstRoots(),
				new StringReader(inFile), new StringWriter());

		// Hacky: refactor this try/catch along with the one in ReferenceServer
		// somehow.
		String operationSummary;
		try {
			interp.execute();
			operationSummary = interp.operationSummary();
		} catch (Interpreter.StaticError err) {
			operationSummary = err.toString();
		} catch (Interpreter.DynamicError err) {
			operationSummary = err.format();
		} catch (ParseFailure pf) {
			operationSummary = pf.toString();
		}

		assertEquals("optimizer", optRef, operationSummary);
	}

	/**
	 * Run the code generator, assemble the resulting .s file, and (if the
	 * output is well-defined) compare against the expected output.
	 */
	public boolean testCodeGenerator() throws IOException {
		// Determine the input and expected output
		String inFile = "";
		if (infile.exists()) {
			FileUtils.readFileToString(infile);
		}

		String binaryFilePath = context.getBinaryFile().getAbsolutePath();
		String execRef = referenceData.getExecutionReference(inFile);

		// Execute the binary file, providing input if relevant, and
		// capturing the output. Check the error code so see if the
		// code signaled dynamic errors.
		String execOut = FileUtil.runCommand(new File("."),
				new String[] { binaryFilePath }, new String[] {}, inFile, true);

		if (RUN_VALGRIND) {
			String[] valgrindCommand = new String[] { "valgrind",
					"--error-exitcode=" + VALGRIND_ERROR_CODE, binaryFilePath };
			String valgrindOut = FileUtil.runCommand(new File("."),
					valgrindCommand, new String[] {}, inFile, true);
			Assert.assertFalse(valgrindOut.contains("Error: "
					+ VALGRIND_ERROR_CODE));
		}

		// Compute the output to what we expected to see.
		return execRef.equals(execOut);
	}

}
