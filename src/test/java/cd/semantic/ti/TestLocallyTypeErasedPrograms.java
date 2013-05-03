package cd.semantic.ti;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import cd.CompilationContext;
import cd.CompilerOptions;
import cd.CompilerOptions.TypeErasureMode;
import cd.CompilerOptions.TypeInferenceMode;
import cd.CompilerToolchain;
import cd.test.TestSamplePrograms;

@RunWith(Parameterized.class)
public class TestLocallyTypeErasedPrograms {
	public static final File TEST_DIR = new File(TestSamplePrograms.class
			.getResource("/ACD_Project/newsyntax/local").getPath());

	private final CompilerToolchain compiler;
	private final CompilationContext compilationContext;
	
	/**
	 * Take all .javali files in the "local" subfolder 
	 * where local type information _may_ be missing
	 */
	@Parameters(name="{index}: {0}")
	public static Collection<Object[]> testFiles() {
		List<Object[]> result = new ArrayList<>();
		IOFileFilter fileFilter = new SuffixFileFilter(".javali");
		IOFileFilter dirFilter = TrueFileFilter.INSTANCE;
		
		if (TEST_DIR != null) {
			for (File file : FileUtils.listFiles(TEST_DIR, fileFilter,
					dirFilter)) {
				result.add(new Object[] { file.getName(),  file });
			}
		}
		return result;
	}
	
	public TestLocallyTypeErasedPrograms(String testName, File file) {
		CompilerOptions options = new CompilerOptions(TypeErasureMode.LOCAL, TypeInferenceMode.LOCAL);
		this.compilationContext = new CompilationContext(file, options);
		this.compiler = CompilerToolchain.forContext(this.compilationContext);
	}
	
	/**
	 * For now, simply run the compiler on the tests with missing local types (all valid programs)
	 * @throws Throwable
	 */
	@Test
	public void test() throws Throwable {
		compiler.compile();
	}
}
