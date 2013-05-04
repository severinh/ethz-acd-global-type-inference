package cd.test;

import java.io.File;
import java.io.IOException;

/**
 * Represents an oracle with regard to how various stages of compilation are
 * meant to react to a certain source file.
 * 
 * The data originates from a reference compiler.
 */
public interface ReferenceData {

	/**
	 * Returns the source file which the reference data is about.
	 * 
	 * @return the source file
	 */
	public File getSourceFile();

	public String getParserReference() throws IOException;

	public String getSemanticReference() throws IOException;

	public String getExecutionReference(String inputText) throws IOException;

	public String getOptimizationReference(String inputText) throws IOException;

}