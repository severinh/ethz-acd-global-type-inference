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

		ReferenceData remoteData, cachedRemoteData;
		remoteData = new RemoteReferenceData(sourceFile);
		cachedRemoteData = new CachedReferenceData(remoteData, "");
		return cachedRemoteData;
	}

}