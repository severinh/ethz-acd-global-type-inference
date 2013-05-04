package cd.test;

import java.io.File;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import org.apache.commons.io.FileUtils;

/**
 * Fetches reference data directly from the remote reference compiler.
 */
public class RemoteReferenceData implements ReferenceData {

	private final File sourceFile;

	public RemoteReferenceData(File sourceFile) {
		super();
		this.sourceFile = sourceFile;
	}

	@Override
	public File getSourceFile() {
		return sourceFile;
	}

	private String getSource() throws IOException {
		return FileUtils.readFileToString(sourceFile);
	}

	@Override
	public String getParserReference() throws IOException {
		Reference ref = openClient();
		try {
			return ref.parserReference(getSource());
		} catch (Throwable e) {
			throw new RuntimeException("Bug in reference solution", e);
		}
	}

	@Override
	public String getSemanticReference() throws IOException {
		Reference ref = openClient();
		try {
			return ref.semanticReference(getSource());
		} catch (Throwable e) {
			throw new RuntimeException("Bug in reference solution", e);
		}
	}

	@Override
	public String getExecutionReference(String inputText) throws IOException {
		Reference ref = openClient();
		try {
			return ref.execReference(getSource(), inputText);
		} catch (Throwable e) {
			throw new RuntimeException("Bug in reference solution", e);
		}
	}

	@Override
	public String getOptimizationReference(String inputText) throws IOException {
		Reference ref = openClient();
		try {
			return ref.optReference(getSource(), inputText);
		} catch (Throwable e) {
			throw new RuntimeException("Bug in reference solution", e);
		}
	}

	/**
	 * Connects to {@code beholder.inf.ethz.ch} and returns a Reference
	 * instance. This uses Java RMI to obtain the expected answer for various
	 * stages. Generally, this is only invoked if no appropriate .ref file is
	 * found.
	 */
	private static Reference openClient() {
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
			// Server not running on beholder.inf.ethz.ch?
			throw new RuntimeException(e);
		}
	}

}
