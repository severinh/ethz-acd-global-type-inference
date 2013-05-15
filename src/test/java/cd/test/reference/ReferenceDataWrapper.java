package cd.test.reference;

import java.io.File;
import java.io.IOException;

import cd.exceptions.ParseFailure;
import cd.exceptions.SemanticFailure;

import com.google.common.base.Optional;

/**
 * Simply wraps a {@link ReferenceData} object.
 * 
 * This class is abstract (because it serves no purpose on its own), but is
 * meant to be the basis for other classes.
 */
public abstract class ReferenceDataWrapper implements ReferenceData {

	private final ReferenceData backingData;

	public ReferenceDataWrapper(ReferenceData backingData) {
		this.backingData = backingData;
	}

	@Override
	public File getSourceFile() {
		return backingData.getSourceFile();
	}

	@Override
	public Optional<ParseFailure> getParseFailure() throws IOException {
		return backingData.getParseFailure();
	}

	@Override
	public Optional<SemanticFailure> getSemanticFailure() throws IOException {
		return backingData.getSemanticFailure();
	}

	@Override
	public String getExecutionReference(String inputText) throws IOException {
		return backingData.getExecutionReference(inputText);
	}

}
