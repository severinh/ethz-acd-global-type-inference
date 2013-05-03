package cd;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The main entrypoint for the compiler. Consists of a series of routines which
 * must be invoked in order. The main() routine here invokes these routines, as
 * does the unit testing code. This is not the <b>best</b> programming practice,
 * as the series of calls to be invoked is duplicated in two places in the code,
 * but it will do for now.
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
