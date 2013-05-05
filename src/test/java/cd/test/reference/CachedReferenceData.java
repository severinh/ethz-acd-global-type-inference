package cd.test.reference;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

/**
 * Wraps a provider of reference data and caches the data for faster access.
 * 
 * For every request for reference data, the class will first check if the data
 * is available locally as a file. If this is the case, the cached data is
 * provided. Otherwise, the data is fetched and cached from the (potentially
 * remote) backing reference data provider.
 */
public class CachedReferenceData extends FileBasedReferenceData {

	private final ReferenceData backingData;

	public CachedReferenceData(ReferenceData backingData) {
		super(backingData.getSourceFile(), "remote");

		this.backingData = backingData;
	}

	@Override
	public String getParserReference() throws IOException {
		// Check for a .ref file
		if (isNewerThanSourceFile(getParserRefFile())) {
			return FileUtils.readFileToString(getParserRefFile());
		}

		// If no file exists, contact reference server
		String result = backingData.getParserReference();
		FileUtils.writeStringToFile(getParserRefFile(), result);

		return result;
	}

	@Override
	public String getSemanticReference() throws IOException {
		// Check for a .ref file
		if (isNewerThanSourceFile(getSemanticRefFile())) {
			return FileUtils.readFileToString(getSemanticRefFile());
		}

		// If no file exists, contact reference server
		String result = backingData.getSemanticReference();
		FileUtils.writeStringToFile(getSemanticRefFile(), result);

		return result;
	}

	@Override
	public String getExecutionReference(String inputText) throws IOException {
		// Check for a .ref file
		if (isNewerThanSourceFile(getExecutionRefFile())) {
			return FileUtils.readFileToString(getExecutionRefFile());
		}

		// If no file exists, use the interpreter to generate one.
		String result = backingData.getExecutionReference(inputText);
		FileUtils.writeStringToFile(getExecutionRefFile(), result);

		return result;
	}

	@Override
	public String getOptimizationReference(String inputText) throws IOException {
		// Check for a .ref file
		if (isNewerThanSourceFile(getOptimizationRefFile())) {
			return FileUtils.readFileToString(getOptimizationRefFile());
		}

		// If no file exists, use the interpreter to generate one.
		String result = backingData.getOptimizationReference(inputText);
		FileUtils.writeStringToFile(getOptimizationRefFile(), result);

		return result;
	}

	private boolean isNewerThanSourceFile(File file) {
		return file.exists()
				&& file.lastModified() > getSourceFile().lastModified();
	}

}
