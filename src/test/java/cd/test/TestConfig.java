package cd.test;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class TestConfig {

	private final boolean isValgrindEnabled;

	public TestConfig() {
		boolean isValgrindEnabled;
		try {
			ResourceBundle bundle = ResourceBundle.getBundle("test");
			isValgrindEnabled = Boolean.valueOf(bundle
					.getString("valgrind.enabled"));
		} catch (MissingResourceException e) {
			isValgrindEnabled = false;
		}
		this.isValgrindEnabled = isValgrindEnabled;
	}

	/**
	 * Whether compiled sample programs should also be run with Valgrind.
	 */
	public boolean isValgrindEnabled() {
		return isValgrindEnabled;
	}

}
