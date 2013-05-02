package cd.test;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class TestLocallyTypeErasedPrograms {
	public static final File TEST_DIR = new File(TestSamplePrograms.class
			.getResource("/").getPath());

	@Parameters(name="{index}: {0}")
	public static Collection<Object[]> testFiles() {
		List<Object[]> result = new ArrayList<>();
		IOFileFilter fileFilter = new SuffixFileFilter(".javali");
		IOFileFilter dirFilter = new NotFileFilter(new NameFileFilter("global"));
		
		if (TEST_DIR != null) {
			for (File file : FileUtils.listFiles(TEST_DIR, fileFilter,
					dirFilter)) {
				result.add(new Object[] { file.getName(),  file });
			}
		}
		return result;
	}
	
	public TestLocallyTypeErasedPrograms(String testName, File file) {
		// TODO Auto-generated constructor stub
	}
}
