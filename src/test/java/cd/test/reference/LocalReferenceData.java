package cd.test.reference;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

/**
 * Provides reference data exclusively from local files. If a file for a certain
 * kind of reference data does not exist, an {@link IOException} will be thrown
 * when a client attempts to access it.
 */
public class LocalReferenceData extends FileBasedReferenceData {

	public LocalReferenceData(File sourceFile, String suffix) {
		super(sourceFile, suffix);
	}

	@Override
	public String getParserReference() throws IOException {
		return FileUtils.readFileToString(getParserRefFile());
	}

	@Override
	public String getSemanticReference() throws IOException {
		// Only return the first line
		String result = FileUtils.readFileToString(getSemanticRefFile());
		String[] lines = result.split("\n");
		return lines[0].trim();
	}

	@Override
	public String getExecutionReference(String inputText) throws IOException {
		return FileUtils.readFileToString(getExecutionRefFile());
	}

}
