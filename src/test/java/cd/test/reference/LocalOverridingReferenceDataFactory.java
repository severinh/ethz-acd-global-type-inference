package cd.test.reference;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.util.ListIterator;

import com.google.common.collect.ImmutableList;

/**
 * A reference data factory where local data always overrides remote data, if
 * present.
 */
public class LocalOverridingReferenceDataFactory extends ReferenceDataFactory {

	private final ReferenceDataFactory remoteFactory;
	private final ImmutableList<String> suffixOrder;

	/**
	 * Constructs a new reference data factory.
	 * 
	 * @param suffixOrder
	 *            If no local reference file with the first specified suffix is
	 *            present, it will look for a reference file with the second
	 *            suffix and so on. If no local reference file with any of the
	 *            suffixes can be found, the remote reference data will be used.
	 */
	public LocalOverridingReferenceDataFactory(ImmutableList<String> suffixOrder) {
		super();

		checkNotNull(suffixOrder);
		checkArgument(!suffixOrder.isEmpty());
		for (String suffix : suffixOrder) {
			checkArgument(!suffix.isEmpty());
		}

		this.remoteFactory = new RemoteReferenceDataFactory();
		this.suffixOrder = suffixOrder;
	}

	@Override
	public ReferenceData of(File sourceFile) {
		checkNotNull(sourceFile);

		ReferenceData localData, result;
		result = remoteFactory.of(sourceFile);
		ListIterator<String> suffixIterator = suffixOrder
				.listIterator(suffixOrder.size());
		// Iterate backwards
		while (suffixIterator.hasPrevious()) {
			String suffix = suffixIterator.previous();
			localData = new LocalReferenceData(sourceFile, suffix);
			result = new FallbackReferenceData(localData, result);
		}
		return result;
	}

}