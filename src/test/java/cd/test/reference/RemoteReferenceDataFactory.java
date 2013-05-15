package cd.test.reference;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;

/**
 * A reference data factory that only uses remote data.
 */
public class RemoteReferenceDataFactory extends ReferenceDataFactory {

	@Override
	public ReferenceData of(File sourceFile) {
		checkNotNull(sourceFile);

		RemoteReferenceData remoteData = new RemoteReferenceData(sourceFile);
		return new CachedReferenceData(remoteData, "");
	}

}