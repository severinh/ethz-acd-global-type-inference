package cd.test.reference;

import java.io.File;

/**
 * A factory that builds the {@link ReferenceData} for given source files.
 * 
 * There are two predefined factories.
 */
public abstract class ReferenceDataFactory {

	public abstract ReferenceData of(File sourceFile);

	/**
	 * Constructs a reference data factory that only uses remote data.
	 */
	public static ReferenceDataFactory makeRemote() {
		return new ReferenceDataFactory() {

			@Override
			public ReferenceData of(File sourceFile) {
				ReferenceData remoteData = new RemoteReferenceData(sourceFile);
				ReferenceData cachedRemoteData = new CachedReferenceData(
						remoteData, "");
				return cachedRemoteData;
			}

		};
	}

	/**
	 * Constructs a reference data factory where local data always overrides
	 * remote data, if present.
	 */
	public static ReferenceDataFactory makeLocalOverridingRemote() {
		return new ReferenceDataFactory() {

			@Override
			public ReferenceData of(File sourceFile) {
				ReferenceData remoteData = new RemoteReferenceData(sourceFile);
				ReferenceData cachedRemoteData = new CachedReferenceData(
						remoteData, "");
				ReferenceData localData = new LocalReferenceData(sourceFile,
						"override");
				return new FallbackReferenceData(localData, cachedRemoteData);
			}

		};
	}

}
