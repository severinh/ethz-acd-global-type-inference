package cd.test.reference;

import java.io.File;
import java.io.IOException;

/**
 * Simply wraps a {@link ReferenceData} object.
 * 
 * This class is abstract (because it serves no purpose on its own), but is
 * meant to be the basis for other classes.
 */
public abstract class ReferenceDataWrapper extends ReferenceData {

	private final ReferenceData backingData;

	public ReferenceDataWrapper(ReferenceData backingData) {
		this.backingData = backingData;
	}

	@Override
	public File getSourceFile() {
		return backingData.getSourceFile();
	}

	@Override
	public String getParserReference() throws IOException {
		return backingData.getParserReference();
	}

	@Override
	public String getSemanticReference() throws IOException {
		return backingData.getSemanticReference();
	}

	@Override
	public String getExecutionReference(String inputText) throws IOException {
		return backingData.getExecutionReference(inputText);
	}

}
