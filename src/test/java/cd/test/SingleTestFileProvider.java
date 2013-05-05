package cd.test;

import java.io.File;
import java.util.Collection;
import java.util.Collections;

public class SingleTestFileProvider implements TestFileProvider {

	private final File testFile;

	public SingleTestFileProvider(File testFile) {
		super();
		this.testFile = testFile;
	}

	@Override
	public Collection<File> getTestFiles() {
		return Collections.singleton(testFile);
	}

}
