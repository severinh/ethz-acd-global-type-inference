package cd.test.reference;

import java.io.File;

/**
 * A factory that builds the {@link ReferenceData} for given source files.
 * 
 * There are two predefined factories.
 */
public abstract class ReferenceDataFactory {

	public abstract ReferenceData of(File sourceFile);

}
