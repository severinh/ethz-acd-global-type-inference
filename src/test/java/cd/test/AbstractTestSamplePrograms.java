package cd.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cd.CompilationContext;
import cd.Config;
import cd.Main;
import cd.debug.AstDump;
import cd.exceptions.AssemblyFailedException;
import cd.exceptions.ParseFailure;
import cd.exceptions.SemanticFailure;
import cd.ir.ast.ClassDecl;
import cd.util.FileUtil;

abstract public class AbstractTestSamplePrograms {

	private static final Logger LOG = LoggerFactory
			.getLogger(AbstractTestSamplePrograms.class);

	private static final boolean RUN_VALGRIND = false;
	/**
	 * We let valgrind return a special exit code if it detect a problem.
	 * Otherwise valgrind returns the exit code of the simulated program.
	 */
	private static final int VALGRIND_ERROR_CODE = 77;

	protected CompilationContext compilation;

	protected File infile;
	protected File errfile;

	protected TestReferenceData referenceData = new TestReferenceData();
	protected Main main;

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
		try (PrintStream err = new PrintStream(errfile)) {
			err.println(String.format(
					"Phase %s failed because we expected to see:", phase));
			err.println(exp);
			err.println("But we actually saw:");
			err.println(act);
			err.println("The difference is:");
			err.println(Diff.computeDiff(exp, act));
		} catch (FileNotFoundException exc) {
			System.err.println("Unable to write debug output to " + errfile
					+ ":");
			exc.printStackTrace();
		}
		Assert.assertEquals(
				String.format("Phase %s for %s failed!", phase, compilation.sourceFile.getPath()),
				exp, act);
	}

	public static int counter = 0;

	@Test
	public void test() throws Throwable {
		System.err.println("[" + counter++ + " = " + compilation.sourceFile + "]");

		// ignore 64-bit-only tests when running 32-bit Java
		if (new File(compilation.sourceFile.getAbsolutePath() + ".64bitonly").exists()
				&& Integer.valueOf(System.getProperty("sun.arch.data.model")) == 32) {
			System.err.println("--> Ignoring test because it's 64-bit-only");
		} else {
			boolean hasWellDefinedOutput = !new File(compilation.sourceFile.getAbsolutePath()
					+ ".undefinedOutput").exists();

			try {
				// Load the input and reference results:
				// Note: this may use the network if no .ref files exist.

				compilation.deleteIntermediateFiles();

				// Parse the file and check that the generated AST is correct,
				// or if the parser failed that the correct message was
				// generated:
				compilation.astRoots = testParser();
				if (compilation.astRoots != null) {
					// Run the semantic check and check that errors
					// are detected correctly, etc.
					boolean passedSemanticAnalysis = testSemanticAnalyzer();
					if (passedSemanticAnalysis) {
						boolean passedCodeGen = testCodeGenerator(hasWellDefinedOutput);

						if (passedCodeGen)
							testOptimizer();
					}
				}
			} catch (org.junit.ComparisonFailure cf) {
				throw cf;
			} catch (Throwable e) {
				try (PrintStream err = new PrintStream(errfile)) {
					err.println("Test failed because an exception was thrown:");
					err.println("    " + e.getLocalizedMessage());
					err.println("Stack trace:");
					e.printStackTrace(err);
				}
				throw e;
			}

			// if we get here, then the test passed, so delete the errfile:
			// (which has been accumulating debug output etc)
			if (errfile.exists())
				errfile.delete();
		}
	}

	/** Run the parser and compare the output against the reference results */
	public List<ClassDecl> testParser() throws Exception {
		String parserRef = referenceData.findParserRef();
		List<ClassDecl> astRoots = null;
		String parserOut;
		boolean parserDebug;

		// parser's debug output is NOT relevant to this assignment.
		// Change to TRUE if you'd like to see it for some reason.
		parserDebug = false;
		try {
			astRoots = main.parse(compilation.sourceFile.getAbsolutePath(), new FileReader(
					this.compilation.sourceFile), parserDebug);
			parserOut = AstDump.toString(astRoots);
		} catch (ParseFailure pf) {
			// Parse errors are ok too.
			LOG.debug("");
			LOG.debug("");
			LOG.debug("{}", pf.toString());
			parserOut = Reference.PARSE_FAILURE;
		}

		// Now that the 2nd assignment is over, we don't
		// do a detailed comparison of the AST, just check
		// whether the parse succeeded or failed.
		if (parserOut.equals(Reference.PARSE_FAILURE)
				|| parserRef.equals(Reference.PARSE_FAILURE))
			assertEquals("parser", parserRef, parserOut);
		return astRoots;
	}

	public boolean testSemanticAnalyzer()
			throws IOException {
		String semanticRef = referenceData.findSemanticRef();

		boolean passed;
		String result;
		try {
			main.semanticCheck(compilation);
			result = "OK";
			passed = true;
		} catch (SemanticFailure sf) {
			result = sf.cause.name();
			LOG.debug("Error message: {}", sf.getLocalizedMessage());
			passed = false;
		}

		assertEquals("semantic", semanticRef, result);
		return passed;
	}

	private void testOptimizer() throws IOException {
		// Determine the input and expected operation counts.
		String inFile = (infile.exists() ? FileUtils.readFileToString(infile)
				: "");
		String optRef = referenceData.findOptimizerRef(inFile);

		// Invoke the interpreter. Don't bother to save the output: we already
		// verified that in testCodeGenerator().
		Interpreter interp = new Interpreter(compilation.astRoots,
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
	public boolean testCodeGenerator(boolean hasWellDefinedOutput) throws IOException {
		// Determine the input and expected output.
		String inFile = (infile.exists() ? FileUtils.readFileToString(infile)
				: "");
		String execRef = referenceData.findExecRef(inFile);

		// Run the code generator:
		try (FileWriter fw = new FileWriter(this.compilation.assemblyFile)) {
			main.generateCode(compilation, fw);
		}

		// At this point, we have generated a .s file and we have to compile
		// it to a binary file. We need to call out to GCC or something
		// to do this.
		String asmOutput = FileUtil.runCommand(
				Config.ASM_DIR,
				Config.ASM,
				new String[] { compilation.binaryFile.getAbsolutePath(),
						compilation.assemblyFile.getAbsolutePath() }, null, false);

		// To check if gcc succeeded, check if the binary file exists.
		// We could use the return code instead, but this seems more
		// portable to other compilers / make systems.
		if (!compilation.binaryFile.exists())
			throw new AssemblyFailedException(asmOutput);

		// Execute the binary file, providing input if relevant, and
		// capturing the output. Check the error code so see if the
		// code signaled dynamic errors.
		String execOut = FileUtil.runCommand(new File("."),
				new String[] { compilation.binaryFile.getAbsolutePath() }, new String[] {},
				inFile, true);

		if (RUN_VALGRIND) {
			String valgrindOut = FileUtil.runCommand(
					new File("."),
					new String[] { "valgrind",
							"--error-exitcode=" + VALGRIND_ERROR_CODE,
							compilation.binaryFile.getAbsolutePath() }, new String[] {},
					inFile, true);
			Assert.assertTrue(!valgrindOut.contains("Error: "
					+ VALGRIND_ERROR_CODE));
		}

		// Compute the output to what we expected to see.
		if (execRef.equals(execOut))
			return true;
		if (hasWellDefinedOutput)
			assertEqualOutput("exec", execRef, execOut);
		return false;
	}



}
