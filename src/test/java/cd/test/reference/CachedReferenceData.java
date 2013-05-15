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

	private final RemoteReferenceData remoteData;

	public CachedReferenceData(RemoteReferenceData remoteData, String suffix) {
		super(remoteData.getSourceFile(), suffix);

		this.remoteData = remoteData;
	}

	@Override
	public String getParseFailureString() throws IOException {
		// Check for a .ref file
		if (isNewerThanSourceFile(getParserRefFile())) {
			return FileUtils.readFileToString(getParserRefFile());
		}

		// If no file exists, contact reference server
		String result = remoteData.getParseFailureString();
		FileUtils.writeStringToFile(getParserRefFile(), result);

		return result;
	}

	@Override
	public String getSemanticFailureString() throws IOException {
		// Check for a .ref file
		if (isNewerThanSourceFile(getSemanticRefFile())) {
			return FileUtils.readFileToString(getSemanticRefFile());
		}

		// If no file exists, contact reference server
		String result = remoteData.getSemanticFailureString();
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
		String result = remoteData.getExecutionReference(inputText);
		FileUtils.writeStringToFile(getExecutionRefFile(), result);

		return result;
	}

	private boolean isNewerThanSourceFile(File file) {
		return file.exists()
				&& file.lastModified() > getSourceFile().lastModified();
	}

}
