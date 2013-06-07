package cd;

import java.io.File;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import cd.exceptions.ParseFailure;
import cd.exceptions.SemanticFailure;

/**
 * The main entry-point of the compiler for stand-alone invocation.
 */
public class Main {

	/**
	 * Parse command line, invoke compile() routine
	 */
	public static void main(String args[]) throws IOException {
		CommandLine line = getCompilerOptions(args);

		CompilerOptions compilerOptions = new CompilerOptions();
		if (line.hasOption("i")) {
			compilerOptions.setTypeInferenceMode(TypeInferenceMode.GLOBAL);
		}
		if (line.hasOption("e")) {
			compilerOptions.setTypeErasureMode(TypeErasureMode.GLOBAL);
		}
		compilerOptions.setDevirtualizing(line.hasOption("o"));
		compilerOptions.setDebugging(line.hasOption("d"));

		for (String file : line.getArgs()) {
			try {
				CompilationContext context = new CompilationContext(new File(
						file), compilerOptions);
				Compiler compiler = Compiler.forContext(context);
				compiler.compile();
			} catch (ParseFailure e) {
				System.err.println(String.format(
						"Could not parse file '%s': %s", file, e.getMessage()));
			} catch (SemanticFailure e) {
				System.err.println(String.format(
						"Semantic check of file '%s' failed: %s", file,
						e.getMessage()));
			}
		}
	}

	@SuppressWarnings("null")
	private static CommandLine getCompilerOptions(String[] args) {
		Options options = new Options();
		options.addOption("i", "infer-types", false, "infer types globally");
		options.addOption("e", "erase-types", false, "erase types globally");
		options.addOption("d", "debug", false, "generate debugging files");
		options.addOption("h", "help", false, "print this message");
		options.addOption("o", "optimize", false, "devirtualize method calls");

		CommandLineParser parser = new GnuParser();
		CommandLine line = null;

		try {
			// Parse the command line arguments
			line = parser.parse(options, args);
		} catch (ParseException exp) {
			// Oops, something went wrong
			System.err.println("Parsing failed.  Reason: " + exp.getMessage());
			System.exit(-1);
		}

		if (line.hasOption("h")) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("javalic", options);
			System.exit(0);
		}

		return line;
	}

}
