package cd.test;

import java.io.File;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import org.apache.commons.io.FileUtils;
import org.codehaus.plexus.util.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestReferenceData {
	private static final Logger LOG = LoggerFactory
			.getLogger(TestReferenceData.class);
	
	public File sourceFile;
	public File semanticreffile;
	public File execreffile;
	public File cfgreffile;
	public File optreffile;
	protected File parserreffile;

	public TestReferenceData() {
	}

	public TestReferenceData(File file) {
		this.sourceFile = file;
		this.parserreffile = new File(file.getPath() + ".parser.ref");
		this.semanticreffile = new File(file.getPath() + ".semantic.ref");
		this.execreffile = new File(file.getPath() + ".exec.ref");
		this.cfgreffile = new File(file.getPath() + ".cfg.dot.ref");
		this.optreffile = new File(file.getPath() + ".opt.ref");	
	}
	
	public String findParserRef() throws IOException {
		// Check for a .ref file
		if (parserreffile.exists()
				&& parserreffile.lastModified() > sourceFile.lastModified()) {
			return FileUtils.readFileToString(parserreffile);
		}

		// If no file exists, contact reference server
		String res;
		Reference ref = openClient();
		try {
			res = ref.parserReference(FileUtils.readFileToString(sourceFile));
		} catch (Throwable e) {
			return fragmentBug(e);
		}
		FileUtils.writeStringToFile(parserreffile, res);
		return res;
	}

	public String findSemanticRef() throws IOException {
		// Semantic ref file is a little different. It consists
		// of 2 lines, but only the first line is significant.
		// The second one contains additional information that we log
		// to the debug file.

		// Read in the result
		String res;
		if (semanticreffile.exists()
				&& semanticreffile.lastModified() > sourceFile.lastModified())
			res = FileUtils.readFileToString(semanticreffile);
		else {
			Reference ref = openClient();
			try {
				res = ref.semanticReference(FileUtils.readFileToString(sourceFile));
			} catch (Throwable e) {
				return fragmentBug(e);
			}
			FileUtils.writeStringToFile(semanticreffile, res);
		}

		// Extract the first line: there should always be multiple lines,
		// but someone may have tinkered with the file or something
		if (res.contains("\n")) {
			int newline = res.indexOf("\n");
			String cause = res.substring(newline + 1);
			if (!cause.equals("") && !cause.equals("\n"))
				LOG.debug("Error message from reference is: {}", cause);
			return res.substring(0, newline); // 1st line
		} else {
			return res;
		}
	}

	public String findExecRef(String inputText) throws IOException {
		// Check for a .ref file
		if (execreffile.exists()
				&& execreffile.lastModified() > sourceFile.lastModified()) {
			return FileUtils.readFileToString(execreffile);
		}

		// If no file exists, use the interpreter to generate one.
		Reference ref = openClient();
		String res;
		try {
			res = ref
					.execReference(FileUtils.readFileToString(sourceFile), inputText);
		} catch (Throwable e) {
			return fragmentBug(e);
		}
		FileUtils.writeStringToFile(execreffile, res);
		return res;
	}

	public String findOptimizerRef(String inputText) throws IOException {
		// Check for a .ref file
		if (optreffile.exists()
				&& optreffile.lastModified() > sourceFile.lastModified()) {
			return FileUtils.readFileToString(optreffile);
		}

		// If no file exists, use the interpreter to generate one.
		Reference ref = openClient();
		String res;
		try {
			res = ref.optReference(FileUtils.readFileToString(sourceFile), inputText);
		} catch (Throwable e) {
			return fragmentBug(e);
		}
		FileUtils.writeStringToFile(optreffile, res);
		return res;
	}

	private String fragmentBug(Throwable e) {
		String res = String.format("** BUG IN REFERENCE SOLUTION: %s **",
				e.toString());
		LOG.debug(res);
		LOG.debug(ExceptionUtils.getStackTrace(e));
		return res;
	}

	/**
	 * Connects to {@code niko.inf.ethz.ch} and returns a Reference instance.
	 * This uses Java RMI to obtain the expected answer for various stages.
	 * Generally, this is only invoked if no appropriate .ref file is found.
	 */
	public static Reference openClient() {
		Registry registry;
		try {
			registry = LocateRegistry.getRegistry("beholder.inf.ethz.ch");
			Reference ref = (Reference) registry.lookup(Reference.class
					.getName());
			return ref;
		} catch (RemoteException e) {
			// No network connectivity or server not running?
			throw new RuntimeException(e);
		} catch (NotBoundException e) {
			// Server not running on niko.inf.ethz.ch?
			throw new RuntimeException(e);
		}
	}
}