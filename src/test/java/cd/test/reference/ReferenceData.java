package cd.test.reference;

import java.io.File;
import java.io.IOException;

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
		return Enums.getIfPresent(Cause.class, getSemanticReference());
	}

	public abstract String getExecutionReference(String inputText)
			throws IOException;

	/**
	 * Constructs a provider of reference data where local data always overrides
	 * remote data, if present.
	 */
	public static ReferenceData localOverridingRemote(File sourceFile) {
		ReferenceData remoteData = new RemoteReferenceData(sourceFile);
		ReferenceData cachedRemoteData = new CachedReferenceData(remoteData, "");
		ReferenceData localData = new LocalReferenceData(sourceFile, "override");
		return new FallbackReferenceData(localData, cachedRemoteData);
	}

}