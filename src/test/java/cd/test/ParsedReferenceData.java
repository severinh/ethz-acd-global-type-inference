package cd.test;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cd.exceptions.SemanticFailure.Cause;

/**
 * Provides methods that transform the wrapped reference data into a more
 * convenient form.
 */
public class ParsedReferenceData implements ReferenceData {

	private static final Logger LOG = LoggerFactory
			.getLogger(ParsedReferenceData.class);

	private final ReferenceData referenceData;

	public ParsedReferenceData(ReferenceData referenceData) {
		super();
		this.referenceData = referenceData;
	}

	@Override
	public File getSourceFile() {
		return referenceData.getSourceFile();
	}

	@Override
	public String getParserReference() throws IOException {
		return referenceData.getParserReference();
	}

	public boolean isParserFailure() throws IOException {
		return getParserReference().equals(Reference.PARSE_FAILURE);
	}

	@Override
	public String getSemanticReference() throws IOException {
		// Semantic ref file is a little different. It consists
		// of 2 lines, but only the first line is significant.
		// The second one contains additional information that we log
		// to the debug file.

		String result = referenceData.getSemanticReference();

		// Extract the first line: there should always be multiple lines,
		// but someone may have tinkered with the file or something
		if (result.contains("\n")) {
			int newline = result.indexOf("\n");
			String cause = result.substring(newline + 1);
			if (!cause.equals("") && !cause.equals("\n")) {
				LOG.debug("Error message from reference is: {}", cause);
			}
			return result.substring(0, newline); // First line
		} else {
			return result;
		}
	}

	public Cause getSemanticFailureCause() throws IOException {
		String result = getSemanticReference();

		if (result.equals(Reference.SEMANTIC_PASSED)) {
			return null;
		} else {
			return Cause.fromString(result);
		}
	}

	@Override
	public String getExecutionReference(String inputText) throws IOException {
		return referenceData.getExecutionReference(inputText);
	}

	@Override
	public String getOptimizationReference(String inputText) throws IOException {
		return referenceData.getOptimizationReference(inputText);
	}

}
