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
 * Performs all end-to-end tests with global type erasure and constraint-based
 * global type inference.
 */
@RunWith(Parameterized.class)
public class GlobalTypeInferenceTest extends TypeInferenceWithConstraintsTest {

	@Parameters(name = "{index}: {0}")
	public static Collection<Object[]> getParameters() {
		TestFileProvider testFileProvider = new RecursiveTestFileProvider(
				TEST_FOLDER);
		CompilerOptions options = new CompilerOptions(TypeErasureMode.GLOBAL,
				TypeInferenceMode.GLOBAL);
		ImmutableList<String> suffixOrder = ImmutableList.of("override.gti",
				"override");
		ReferenceDataFactory factory = new LocalOverridingReferenceDataFactory(
				suffixOrder, new CustomRemoteReferenceDataFactory());
		return buildParameters(testFileProvider, options, factory);
	}

	public GlobalTypeInferenceTest(String testName, @Nonnull File file,
			@Nonnull CompilerOptions options, ReferenceData referenceData) {
		super(testName, file, options, referenceData);
	}

}
