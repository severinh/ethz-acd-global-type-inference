package cd.ir.symbols;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import cd.ir.ast.ClassDecl;

public class ClassSymbol extends TypeSymbol {

	public final ClassDecl ast;
	public ClassSymbol superClass;
	public final VariableSymbol thisSymbol = new VariableSymbol("this", this);
	public final Map<String, VariableSymbol> fields = new HashMap<>();
	public final Map<String, MethodSymbol> methods = new HashMap<>();

	public int totalMethods = -1;
	public int totalFields = -1;
	public int sizeof = -1;

	public ClassSymbol(ClassDecl ast) {
		super(ast.name);
		this.ast = ast;
	}

	/**
	 * Used to create the default {@code Object} and {@code <null>} types
	 */
	public ClassSymbol(String name) {
		super(name);
		this.ast = null;
	}

	@Override
	public boolean isReferenceType() {
		return true;
	}

	public VariableSymbol getField(String name) {
		VariableSymbol fsym = fields.get(name);
		if (fsym == null && superClass != null)
			return superClass.getField(name);
		return fsym;
	}

	public MethodSymbol getMethod(String name) {
		MethodSymbol msym = methods.get(name);
		if (msym == null && superClass != null)
			return superClass.getMethod(name);
		return msym;
	}

	public Collection<VariableSymbol> getFields() {
		return Collections.unmodifiableCollection(fields.values());
	}

	public Collection<MethodSymbol> getMethods() {
		return Collections.unmodifiableCollection(methods.values());
	}

}