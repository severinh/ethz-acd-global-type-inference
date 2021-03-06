package cd.test.reference;

import java.io.File;

/**
 * Abstract base class for reference data providers that utilize a file for each
 * of the several kinds of reference data.
 */
public abstract class FileBasedReferenceData extends RawReferenceData {

	private final File sourceFile;

	private final File parserRefFile;
	private final File semanticRefFile;
	private final File executionRefFile;

	public FileBasedReferenceData(File sourceFile, String suffix) {
		super();

		this.sourceFile = sourceFile;

		if (!suffix.isEmpty()) {
			suffix = "." + suffix;
		}
		String path = sourceFile.getPath();
		parserRefFile = new File(path + ".parser.ref" + suffix);
		semanticRefFile = new File(path + ".semantic.ref" + suffix);
		executionRefFile = new File(path + ".exec.ref" + suffix);
	}

	@Override
	public File getSourceFile() {
		return sourceFile;
	}

	protected File getParserRefFile() {
		return parserRefFile;
	}

	protected File getSemanticRefFile() {
		return semanticRefFile;
	}

	protected File getExecutionRefFile() {
		return executionRefFile;
	}

}
