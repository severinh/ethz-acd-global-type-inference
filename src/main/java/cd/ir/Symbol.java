package cd.ir;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cd.codegen.AstCodeGenerator;

public abstract class Symbol {

	public final String name;

	public static abstract class TypeSymbol extends Symbol {

		public TypeSymbol(String name) {
			super(name);
		}

		public abstract boolean isReferenceType();

		@Override
		public String toString() {
			return name;
		}

	}

	public static class PrimitiveTypeSymbol extends TypeSymbol {
		public PrimitiveTypeSymbol(String name) {
			super(name);
		}

		@Override
		public boolean isReferenceType() {
			return false;
		}
	}

	public static class ArrayTypeSymbol extends TypeSymbol {
		public final TypeSymbol elementType;

		public ArrayTypeSymbol(TypeSymbol elementType) {
			super(elementType.name + "[]");
			this.elementType = elementType;
		}

		@Override
		public boolean isReferenceType() {
			return true;
		}
	}

	public static class ClassSymbol extends TypeSymbol {

		public final Ast.ClassDecl ast;
		public ClassSymbol superClass;
		public final VariableSymbol thisSymbol = new VariableSymbol("this",
				this);
		public final Map<String, VariableSymbol> fields = new HashMap<String, VariableSymbol>();
		public final Map<String, MethodSymbol> methods = new HashMap<String, MethodSymbol>();

		public int totalMethods = -1;
		public int totalFields = -1;
		public int sizeof = -1;

		public ClassSymbol(Ast.ClassDecl ast) {
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
	}

	public static class MethodSymbol extends Symbol {

		public final Ast.MethodDecl ast;
		public final Map<String, VariableSymbol> locals = new HashMap<String, VariableSymbol>();
		public final List<VariableSymbol> parameters = new ArrayList<VariableSymbol>();

		public TypeSymbol returnType;

		public final ClassSymbol owner;

		public int vtableIndex = -1;

		public MethodSymbol overrides;

		public MethodSymbol(Ast.MethodDecl ast, ClassSymbol owner) {
			super(ast.name);
			this.ast = ast;
			this.owner = owner;
		}

		@Override
		public String toString() {
			return name + "(...)";
		}

	}

	public static class VariableSymbol extends Symbol {

		public static enum Kind {
			PARAM, LOCAL, FIELD
		};

		public final TypeSymbol type;
		public final Kind kind;
		public final int version;

		/**
		 * Meaning depends on the kind of variable, but generally refers to the
		 * offset in bytes from some base ptr to where the variable is found.
		 * <ul>
		 * <li>{@code PARAM}, {@code LOCAL}: Offset from BP
		 * <li>{@code FIELD}: Offset from object
		 * <li>{@code CONSTANT}: N/A
		 * </ul>
		 * Computed in {@link AstCodeGenerator}.
		 */
		public int offset;

		public VariableSymbol(VariableSymbol v0sym, int version) {
			super(v0sym.name + "_" + version);
			this.type = v0sym.type;
			this.kind = v0sym.kind;
			this.offset = v0sym.offset;
			this.version = version;
		}

		public VariableSymbol(String name, TypeSymbol type) {
			this(name, type, Kind.PARAM);
		}

		public VariableSymbol(String name, TypeSymbol type, Kind kind) {
			super(name);
			this.type = type;
			this.kind = kind;
			this.version = 0;
			this.offset = -1;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	protected Symbol(String name) {
		this.name = name;
	}

}
