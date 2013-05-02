package cd.test;

import java.io.File;

public class TestReferenceData {
	public File semanticreffile;
	public File execreffile;
	public File cfgreffile;
	public File optreffile;
	protected File parserreffile;

	public TestReferenceData() {
	}

	public TestReferenceData(File file) {
		this.parserreffile = new File(file.getPath() + ".parser.ref");
		this.semanticreffile = new File(file.getPath() + ".semantic.ref");
		this.execreffile = new File(file.getPath() + ".exec.ref");
		this.cfgreffile = new File(file.getPath() + ".cfg.dot.ref");
		this.optreffile = new File(file.getPath() + ".opt.ref");	}
}