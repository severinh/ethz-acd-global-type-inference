package cd;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The main entrypoint of the compiler for standalone invocation (not in tests)
 */
public class Main {

	public static final Logger LOG = LoggerFactory.getLogger(Main.class);

	/** Parse command line, invoke compile() routine */
	public static void main(String args[]) throws IOException {
		for (String file : args) {
			CompilationContext compilationContext = new CompilationContext(new File(file));
			CompilerToolchain compiler = CompilerToolchain.forContext(compilationContext);
			compiler.compile();
		}
	}
}
