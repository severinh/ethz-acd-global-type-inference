package cd.test;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import javax.annotation.Nonnull;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import cd.CompilerOptions;
import cd.TypeErasureMode;
import cd.TypeInferenceMode;
import cd.exceptions.SemanticFailure;
import cd.exceptions.SemanticFailure.Cause;
import cd.test.fileprovider.RecursiveTestFileProvider;
import cd.test.fileprovider.TestFileProvider;
import cd.test.reference.LocalOverridingReferenceDataFactory;
import cd.test.reference.ReferenceData;
import cd.test.reference.ReferenceDataFactory;
import cd.test.reference.ReferenceDataWrapper;
import cd.test.reference.RemoteReferenceDataFactory;

/**
 * Performs all end-to-end tests with local type erasure and constraint-based
 * local type inference.
 */
@RunWith(Parameterized.class)
public class LocalTypeInferenceWithConstraintsTest extends TestSamplePrograms {

	@Parameters(name = "{index}: {0}")
	public static Collection<Object[]> getParameters() {
		TestFileProvider testFileProvider = RecursiveTestFileProvider
				.withExcludedDir(TEST_FOLDER, "global");
		CompilerOptions options = new CompilerOptions(TypeErasureMode.LOCAL,
				TypeInferenceMode.LOCAL_CONSTRAINTS);
		ImmutableList<String> suffixOrder = ImmutableList.of("override.ltiwc",
				"override");
		ReferenceDataFactory factory = new LocalOverridingReferenceDataFactory(
				suffixOrder, new CustomRemoteReferenceDataFactory());
		return buildParameters(testFileProvider, options, factory);
	}

	public LocalTypeInferenceWithConstraintsTest(String testName,
			@Nonnull File file, @Nonnull CompilerOptions options,
			ReferenceData referenceData) {
		super(testName, file, options, referenceData);
	}

	/**
	 * Custom reference data factory that uses local overrides if present and
	 * remote data otherwise.
	 * 
	 * What is special about it is that the method
	 * {@code ReferenceData#getSemanticFailureCause()} of the created
	 * {@code ReferenceData} objects will always return a
	 * {@code Cause#TYPE_INFERENCE_ERROR} rather than a {@link Cause#TYPE_ERROR}
	 */
	private static class CustomRemoteReferenceDataFactory extends
			RemoteReferenceDataFactory {

		@Override
		public ReferenceData of(File sourceFile) {
			ReferenceData result = super.of(sourceFile);
			result = new RemoteReferenceDataWrapper(result);
			return result;
		}

	}

	private static class RemoteReferenceDataWrapper extends
			ReferenceDataWrapper {

		public RemoteReferenceDataWrapper(ReferenceData backingData) {
			super(backingData);
		}

		@Override
		public Optional<SemanticFailure> getSemanticFailure()
				throws IOException {
			Optional<SemanticFailure> semanticFailure = super
					.getSemanticFailure();
			if (semanticFailure.isPresent()) {
				switch (semanticFailure.get().cause) {
				case TYPE_ERROR:
				case NO_SUCH_FIELD:
					return Optional.of(new SemanticFailure(
							Cause.TYPE_INFERENCE_ERROR));
				default:
					break;
				}
			}
			return semanticFailure;
		}

	}

}
