package cd.test.reference;

import java.io.File;
import java.io.IOException;

import com.google.common.base.Optional;

import cd.exceptions.ParseFailure;
import cd.exceptions.SemanticFailure;

/**
 * Represents an oracle with regard to how various stages of compilation are
 * meant to react to a certain source file.
 */
public interface ReferenceData {

	/**
	 * Returns the source file which the reference data is about.
	 * 
	 * @return the source file
	 */
	public abstract File getSourceFile();

	public abstract Optional<ParseFailure> getParseFailure() throws IOException;

	public abstract Optional<SemanticFailure> getSemanticFailure()
			throws IOException;

	public abstract String getExecutionReference(String inputText)
			throws IOException;

}