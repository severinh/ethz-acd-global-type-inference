package cd.test;

import java.io.File;
import java.io.IOException;

import cd.exceptions.SemanticFailure.Cause;

/**
 * Represents an oracle with regard to how various stages of compilation are
 * meant to react to a certain source file.
 * 
 * The data originates from a reference compiler.
 */
public abstract class ReferenceData {

	/**
	 * Returns the source file which the reference data is about.
	 * 
	 * @return the source file
	 */
	public abstract File getSourceFile();

	public abstract String getParserReference() throws IOException;

	public boolean isParseFailure() throws IOException {
		return getParserReference().equals(Reference.PARSE_FAILURE);
	}

	public abstract String getSemanticReference() throws IOException;

	public Cause getSemanticFailureCause() throws IOException {
		String result = getSemanticReference();

		if (result.equals(Reference.SEMANTIC_PASSED)) {
			return null;
		} else {
			return Cause.fromString(result);
		}
	}

	public abstract String getExecutionReference(String inputText)
			throws IOException;

	public abstract String getOptimizationReference(String inputText)
			throws IOException;

}