package cd.test.reference;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang.StringUtils;

import com.google.common.base.Enums;
import com.google.common.base.Optional;

import cd.exceptions.SemanticFailure.Cause;
import cd.test.Reference;

/**
 * Represents an oracle with regard to how various stages of compilation are
 * meant to react to a certain source file.
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

	public Optional<Cause> getSemanticFailureCause() throws IOException {
		// If there are multiple lines, only use the first one to determine the
		// semantic failure cause
		String[] lines = StringUtils.split(getSemanticReference(), "\n");
		String causeString = lines[0];
		Optional<Cause> cause = Enums.getIfPresent(Cause.class, causeString);
		return cause;
	}

	public abstract String getExecutionReference(String inputText)
			throws IOException;

}