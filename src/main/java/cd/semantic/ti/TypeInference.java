package cd.semantic.ti;

import cd.CompilationContext;
import cd.util.NonnullByDefault;

@NonnullByDefault
public interface TypeInference {

	public void inferTypes(CompilationContext context);

}
