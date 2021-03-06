package cd.test.reference;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;

import cd.exceptions.ParseFailure;
import cd.exceptions.SemanticFailure;

import com.google.common.base.Optional;

/**
 * Wraps two providers of reference data for the same source file, a primary and
 * a secondary one. When accessing certain reference data from the primary one
 * fails, the class uses the secondary one instead.
 * 
 * In other words, this makes it possible for the primary source of reference
 * data to <em>override</em> the secondary source of reference data.
 */
public class FallbackReferenceData implements ReferenceData {

	private final ReferenceData primaryData;
	private final ReferenceData secondaryData;

	public FallbackReferenceData(ReferenceData primaryData,
			ReferenceData secondaryData) {
		super();

		this.primaryData = primaryData;
		this.secondaryData = secondaryData;

		Assert.assertEquals(primaryData.getSourceFile(),
				secondaryData.getSourceFile());
	}

	@Override
	public File getSourceFile() {
		return primaryData.getSourceFile();
	}

	@Override
	public Optional<ParseFailure> getParseFailure() throws IOException {
		try {
			return primaryData.getParseFailure();
		} catch (IOException e) {
			return secondaryData.getParseFailure();
		}
	}

	@Override
	public Optional<SemanticFailure> getSemanticFailure() throws IOException {
		try {
			return primaryData.getSemanticFailure();
		} catch (IOException e) {
			return secondaryData.getSemanticFailure();
		}
	}

	@Override
	public String getExecutionReference(String inputText) throws IOException {
		try {
			return primaryData.getExecutionReference(inputText);
		} catch (IOException e) {
			return secondaryData.getExecutionReference(inputText);
		}
	}

}
