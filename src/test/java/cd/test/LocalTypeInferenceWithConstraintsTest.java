package cd.test;

import java.io.File;
import java.util.Collection;

import javax.annotation.Nonnull;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.google.common.collect.ImmutableList;

import cd.CompilerOptions;
import cd.TypeErasureMode;
import cd.TypeInferenceMode;
import cd.test.fileprovider.RecursiveTestFileProvider;
import cd.test.fileprovider.TestFileProvider;
import cd.test.reference.LocalOverridingReferenceDataFactory;
import cd.test.reference.ReferenceData;
import cd.test.reference.ReferenceDataFactory;

/**
 * Performs all end-to-end tests with local type erasure and constraint-based
 * local type inference.
 */
@RunWith(Parameterized.class)
public class LocalTypeInferenceWithConstraintsTest extends
		TypeInferenceWithConstraintsTest {

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

}
