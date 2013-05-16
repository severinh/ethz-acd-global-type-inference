package cd.test;

import java.io.File;
import java.io.IOException;

import javax.annotation.Nonnull;

import cd.CompilerOptions;
import cd.exceptions.SemanticFailure;
import cd.exceptions.SemanticFailure.Cause;
import cd.test.reference.ReferenceData;
import cd.test.reference.ReferenceDataWrapper;
import cd.test.reference.RemoteReferenceDataFactory;

import com.google.common.base.Optional;

public abstract class TypeInferenceWithConstraintsTest extends TestSamplePrograms {

	public TypeInferenceWithConstraintsTest(String testName,
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
	static class CustomRemoteReferenceDataFactory extends
			RemoteReferenceDataFactory {

		@Override
		public ReferenceData of(File sourceFile) {
			ReferenceData result = super.of(sourceFile);
			result = new RemoteReferenceDataWrapper(result);
			return result;
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

}
