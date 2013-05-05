package cd.test.reference;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;

/**
 * Wraps two providers of reference data for the same source file, a primary and
 * a secondary one. When accessing certain reference data from the primary one
 * fails, the class uses the secondary one instead.
 * 
 * In other words, this makes it possible for the primary source of reference
 * data to <em>override</em> the secondary source of reference data.
 */
public class FallbackReferenceData extends ReferenceData {

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

	/**
	 * Constructs a provider of reference data where local data always overrides
	 * remote data, if present.
	 */
	public static FallbackReferenceData makeLocalDominatingRemote(
			File sourceFile) {
		ReferenceData remoteData = RemoteReferenceData.makeCached(sourceFile);
		ReferenceData localData = new LocalReferenceData(sourceFile);
		return new FallbackReferenceData(localData, remoteData);
	}

	@Override
	public File getSourceFile() {
		return primaryData.getSourceFile();
	}

	@Override
	public String getParserReference() throws IOException {
		try {
			return primaryData.getParserReference();
		} catch (IOException e) {
			return secondaryData.getParserReference();
		}
	}

	@Override
	public String getSemanticReference() throws IOException {
		try {
			return primaryData.getSemanticReference();
		} catch (IOException e) {
			return secondaryData.getSemanticReference();
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

	@Override
	public String getOptimizationReference(String inputText) throws IOException {
		try {
			return primaryData.getOptimizationReference(inputText);
		} catch (IOException e) {
			return secondaryData.getOptimizationReference(inputText);
		}
	}

}
