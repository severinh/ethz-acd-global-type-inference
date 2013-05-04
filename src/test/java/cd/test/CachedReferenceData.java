package cd.test;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

/**
 * Wraps a provider of reference data and caches the data for faster access.
 */
public class CachedReferenceData implements ReferenceData {

	private final ReferenceData backingData;

	private final File semanticReferenceFile;
	private final File executionReferenceFile;
	private final File optimizationReferenceFile;
	private final File parserReferenceFile;

	public CachedReferenceData(ReferenceData backingData) {
		super();

		String sourcePath = backingData.getSourceFile().getPath();

		this.backingData = backingData;
		this.parserReferenceFile = new File(sourcePath + ".parser.ref");
		this.semanticReferenceFile = new File(sourcePath + ".semantic.ref");
		this.executionReferenceFile = new File(sourcePath + ".exec.ref");
		this.optimizationReferenceFile = new File(sourcePath + ".opt.ref");
	}

	@Override
	public File getSourceFile() {
		return backingData.getSourceFile();
	}

	@Override
	public String getParserReference() throws IOException {
		// Check for a .ref file
		if (parserReferenceFile.exists()
				&& parserReferenceFile.lastModified() > getSourceFile()
						.lastModified()) {
			return FileUtils.readFileToString(parserReferenceFile);
		}

		// If no file exists, contact reference server
		String result = backingData.getParserReference();
		FileUtils.writeStringToFile(parserReferenceFile, result);

		return result;
	}

	@Override
	public String getSemanticReference() throws IOException {
		// Check for a .ref file
		if (isNewerThanSourceFile(semanticReferenceFile)) {
			return FileUtils.readFileToString(semanticReferenceFile);
		}

		String result = backingData.getSemanticReference();
		FileUtils.writeStringToFile(semanticReferenceFile, result);

		return result;
	}

	@Override
	public String getExecutionReference(String inputText) throws IOException {
		// Check for a .ref file
		if (isNewerThanSourceFile(executionReferenceFile)) {
			return FileUtils.readFileToString(executionReferenceFile);
		}

		// If no file exists, use the interpreter to generate one.
		String result = backingData.getExecutionReference(inputText);
		FileUtils.writeStringToFile(executionReferenceFile, result);

		return result;
	}

	@Override
	public String getOptimizationReference(String inputText) throws IOException {
		// Check for a .ref file
		if (isNewerThanSourceFile(optimizationReferenceFile)) {
			return FileUtils.readFileToString(optimizationReferenceFile);
		}

		// If no file exists, use the interpreter to generate one.
		String result = backingData.getOptimizationReference(inputText);
		FileUtils.writeStringToFile(optimizationReferenceFile, result);

		return result;
	}

	private boolean isNewerThanSourceFile(File file) {
		return file.exists()
				&& file.lastModified() > getSourceFile().lastModified();
	}

}
