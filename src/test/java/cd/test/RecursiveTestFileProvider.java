package cd.test;

import java.io.File;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;

public class RecursiveTestFileProvider implements TestFileProvider {

	private final File rootFolder;
	private final IOFileFilter dirFilter;

	public RecursiveTestFileProvider(File rootFolder, IOFileFilter dirFilter) {
		super();
		this.rootFolder = rootFolder;
		this.dirFilter = dirFilter;
	}

	/**
	 * Constructs a recursive test file provider that excludes any file whose
	 * path contains a directory of a given name.
	 * 
	 * @param rootFolder
	 *            the root folder to search from recursively
	 * @param dir
	 *            the name of the directory to exclude
	 * @return
	 */
	public static RecursiveTestFileProvider withExcludedDir(File rootFolder,
			String dir) {
		IOFileFilter dirFilter = new NotFileFilter(new NameFileFilter(dir));
		return new RecursiveTestFileProvider(rootFolder, dirFilter);
	}

	@Override
	public Collection<File> getTestFiles() {
		IOFileFilter fileFilter = new SuffixFileFilter(".javali");
		return FileUtils.listFiles(rootFolder, fileFilter, dirFilter);
	}

}
