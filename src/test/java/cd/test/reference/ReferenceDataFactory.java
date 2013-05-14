package cd.test.reference;

import java.io.File;
import java.util.ListIterator;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkArgument;
import com.google.common.collect.ImmutableList;

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
		return new RemoteFactory();
	}

	/**
	 * Constructs a reference data factory where local data always overrides
	 * remote data, if present.
	 */
	public static ReferenceDataFactory makeLocalOverridingRemote(
			ImmutableList<String> suffixOrder) {
		checkNotNull(suffixOrder);
		checkArgument(!suffixOrder.isEmpty());

		return new LocalOverridingRemoteFactory(suffixOrder);
	}

	private static class RemoteFactory extends ReferenceDataFactory {

		@Override
		public ReferenceData of(File sourceFile) {
			checkNotNull(sourceFile);

			ReferenceData remoteData, cachedRemoteData;
			remoteData = new RemoteReferenceData(sourceFile);
			cachedRemoteData = new CachedReferenceData(remoteData, "");
			return cachedRemoteData;
		}

	}

	private static class LocalOverridingRemoteFactory extends
			ReferenceDataFactory {

		private final ReferenceDataFactory remoteFactory = makeRemote();
		private final ImmutableList<String> suffixOrder;

		public LocalOverridingRemoteFactory(ImmutableList<String> suffixOrder) {
			super();

			checkNotNull(suffixOrder);
			checkArgument(!suffixOrder.isEmpty());
			for (String suffix : suffixOrder) {
				checkArgument(!suffix.isEmpty());
			}

			this.suffixOrder = suffixOrder;
		}

		@Override
		public ReferenceData of(File sourceFile) {
			checkNotNull(sourceFile);

			ReferenceData localData, result;
			result = remoteFactory.of(sourceFile);
			ListIterator<String> suffixIterator = suffixOrder
					.listIterator(suffixOrder.size());
			while (suffixIterator.hasPrevious()) {
				String suffix = suffixIterator.previous();
				localData = new LocalReferenceData(sourceFile, suffix);
				result = new FallbackReferenceData(localData, result);
			}
			return result;
		}

	}

}
