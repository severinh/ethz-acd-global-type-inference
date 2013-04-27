package cd.test;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import cd.ir.ast.Assign;
import cd.ir.ast.Ast;
import cd.ir.ast.BinaryOp;
import cd.ir.ast.BooleanConst;
import cd.ir.ast.BuiltInRead;
import cd.ir.ast.BuiltInReadFloat;
import cd.ir.ast.BuiltInWrite;
import cd.ir.ast.BuiltInWriteFloat;
import cd.ir.ast.BuiltInWriteln;
import cd.ir.ast.Cast;
import cd.ir.ast.ClassDecl;
import cd.ir.ast.Field;
import cd.ir.ast.FloatConst;
import cd.ir.ast.IfElse;
import cd.ir.ast.Index;
import cd.ir.ast.IntConst;
import cd.ir.ast.MethodCall;
import cd.ir.ast.MethodCallExpr;
import cd.ir.ast.MethodDecl;
import cd.ir.ast.NewArray;
import cd.ir.ast.NewObject;
import cd.ir.ast.NullConst;
import cd.ir.ast.ReturnStmt;
import cd.ir.ast.ThisRef;
import cd.ir.ast.UnaryOp;
import cd.ir.ast.Var;
import cd.ir.ast.WhileLoop;
import cd.ir.symbols.VariableSymbol;
import cd.ir.AstVisitor;
import cd.ir.BasicBlock;
import cd.ir.ControlFlowGraph;
import cd.util.FileUtil;

/**
 * An interpreter for the Javali IR. It requires that the IR be fully
 * semantically analyzed -- in particular, that symbols be assigned to Var and
 * Field nodes.
 * 
 * It can interpret either the AST used in CD1 or the CFG from CD2. It detects
 * infinite loops and also tracks how many operations of each kind were
 * performed.
 */
public class Interpreter {

	private static final int MAX_STEPS = 1000000;

	private int steps = 0;
	private final JlNull nullPointer = new JlNull();

	private final List<ClassDecl> classDecls;
	private final Writer output;
	private final Scanner input;

	public Interpreter(List<ClassDecl> classDecls, Reader in, Writer out) {
		this.classDecls = classDecls;
		this.input = new Scanner(in);
		this.output = out;
	}

	public void execute() {
		invokeMethod("Main", "main", new JlObject("Main"),
				Collections.<JlValue> emptyList());
	}

	// Optimization detection:
	//
	// We count the number of binary and unary operations that
	// occurred during execution and compare this to a fully-optimized version.
	// The key to this hashtable is either a BinaryOp.BOp or UnaryOp.UOp.
	private final Map<Object, Integer> opCounts = new HashMap<>();

	private void increment(Object operator) {
		Integer current = opCounts.get(operator);
		if (current == null)
			opCounts.put(operator, 1);
		else
			opCounts.put(operator, current + 1);
	}

	public String operationSummary() {
		List<String> operationSummaries = new ArrayList<>();

		for (Object operation : opCounts.keySet()) {
			operationSummaries.add(String.format("%s: %s\n", operation,
					opCounts.get(operation)));
		}

		Collections.sort(operationSummaries);

		StringBuilder sb = new StringBuilder();
		for (String summary : operationSummaries) {
			sb.append(summary);
		}

		return sb.toString();
	}

	/** Thrown in cases that should be ruled out by static analysis: */
	@SuppressWarnings("serial")
	class StaticError extends RuntimeException {

		public StaticError(String message) {
			super(message);
		}

	}

	/** Thrown in cases that are not ruled out by static analysis: */
	@SuppressWarnings("serial")
	class DynamicError extends RuntimeException {

		public static final int INVALID_DOWNCAST = 1;
		public static final int INVALID_ARRAY_STORE = 2;
		public static final int INVALID_ARRAY_BOUNDS = 3;
		public static final int NULL_POINTER = 4;
		public static final int INVALID_ARRAY_SIZE = 5;
		public static final int UNINITIALIZED_VALUE = 6;
		public static final int INFINITE_LOOP = 7;
		public static final int DIVISION_BY_ZERO = 8;
		public static final int INTERNAL_ERROR = 22;

		private final int code;

		public DynamicError(String message, int code) {
			super(message);
			this.code = code;
		}

		public DynamicError(Throwable thr, int code) {
			super(thr);
			this.code = code;
		}

		/**
		 * Returns a string exactly like the one that
		 * {@link FileUtil#runCommand(java.io.File, String[], String[], String, boolean)}
		 * returns when a command results in an error.
		 */
		public String format() {
			return "Error: " + Integer.toString(code) + "\n";
		}

	}

	// Values:
	abstract class JlValue {
		public final String typeName;

		public JlValue(String s) {
			typeName = s;
		}

		public boolean asBoolean() {
			throw new StaticError("Not a bool");
		}

		public int asInt() {
			throw new StaticError("Not an int");
		}

		public float asFloat() {
			throw new StaticError("Not an float");
		}

		public JlReference asRef() {
			throw new StaticError("Not a reference");
		}

		public JlObject asObject() {
			throw new StaticError("Not an object");
		}

		// Binary Operations
		// Arithmetic
		public JlValue times(JlValue value) {
			throw new StaticError("* Operation not supported");
		}

		public JlValue div(JlValue value) {
			throw new StaticError("/ Operation not supported");
		}

		public JlValue add(JlValue value) {
			throw new StaticError("+ Operation not supported");
		}

		public JlValue subtract(JlValue value) {
			throw new StaticError("- Operation not supported");
		}

		public JlValue mod(JlValue value) {
			throw new StaticError("% Operation not supported");
		}

		// Boolean
		public JlValue or(JlValue value) {
			throw new StaticError("&& Operation not supported");
		}

		public JlValue and(JlValue value) {
			throw new StaticError("|| Operation not supported");
		}

		// Comparison
		public JlValue less(JlValue value) {
			throw new StaticError("< Operation not supported");
		}

		public JlValue lessOrEqual(JlValue value) {
			throw new StaticError("<= Operation not supported");
		}

		public JlValue greater(JlValue value) {
			throw new StaticError("> Operation not supported");
		}

		public JlValue greaterOrEqual(JlValue value) {
			throw new StaticError(">= Operation not supported");
		}

		// Unary Operation
		public JlValue plus() {
			throw new StaticError("+ Operation not supported");
		}

		public JlValue minus() {
			throw new StaticError("- Operation not supported");
		}

		public JlValue not() {
			throw new StaticError("not Operation not supported");
		}
	}

	abstract class JlReference extends JlValue {

		public JlReference(String s) {
			super(s);
		}

		@Override
		public JlReference asRef() {
			return this;
		}

		public abstract JlValue field(VariableSymbol name);

		public abstract void setField(VariableSymbol name, JlValue val);

		public abstract JlValue deref(int index);

		public abstract void setDeref(int index, JlValue val);

		public abstract boolean canBeCastTo(String typeName);

		@Override
		public String toString() {
			return String.format("%s(%x)", typeName,
					System.identityHashCode(this));
		}

	}

	class JlObject extends JlReference {

		protected Map<VariableSymbol, JlValue> fields = new HashMap<>();

		public JlObject(String s) {
			super(s);
		}

		@Override
		public JlObject asObject() {
			return this;
		}

		@Override
		public JlValue field(VariableSymbol name) {

			if (fields.containsKey(name)) {
				return fields.get(name);
			}

			return new JlUninitialized();

		}

		@Override
		public void setField(VariableSymbol name, JlValue val) {
			fields.put(name, val);
		}

		@Override
		public JlValue deref(int index) {
			throw new StaticError("Not an array");
		}

		@Override
		public void setDeref(int index, JlValue val) {
			throw new StaticError("Not an array");
		}

		@Override
		public boolean canBeCastTo(String typeName) {

			// Can always cast to Object:
			if (typeName.equals("Object"))
				return true;

			// Make up a set of acceptable types. Check for circular loops!
			Set<String> superTypes = new HashSet<>();
			String currentType = this.typeName;

			while (!currentType.equals("Object")) {

				if (superTypes.contains(currentType)) {
					throw new StaticError("Circular inheritance: "
							+ currentType);
				}

				superTypes.add(currentType);
				ClassDecl cd = findClassDecl(currentType);
				currentType = cd.superClass;
			}

			return superTypes.contains(typeName);

		}

	}

	class JlArray extends JlReference {

		private final JlValue contents[];

		public JlArray(String s, int size) {

			super(s);

			if (size < 0) {
				throw new DynamicError("Invalid array size: " + size,
						DynamicError.INVALID_ARRAY_SIZE);
			}

			this.contents = new JlValue[size];
			for (int i = 0; i < size; i++) {
				this.contents[i] = new JlUninitialized();
			}

		}

		@Override
		public JlReference asRef() {
			return this;
		}

		public JlArray asArray() {
			return this;
		}

		@Override
		public JlValue deref(int idx) {

			try {
				return contents[idx];
			} catch (final ArrayIndexOutOfBoundsException ex) {
				throw new DynamicError("Array index out of bounds " + idx,
						DynamicError.INVALID_ARRAY_BOUNDS);
			}

		}

		@Override
		public void setDeref(int idx, JlValue value) {

			try {
				contents[idx] = value;
			} catch (final ArrayIndexOutOfBoundsException ex) {
				throw new DynamicError("Array index out of bounds " + idx,
						DynamicError.INVALID_ARRAY_BOUNDS);
			}

		}

		@Override
		public JlValue field(VariableSymbol name) {
			throw new StaticError("Not an object");
		}

		@Override
		public void setField(VariableSymbol name, JlValue value) {
			throw new StaticError("Not an object");
		}

		@Override
		public boolean canBeCastTo(String typeName) {
			return this.typeName.equals(typeName) || typeName.equals("Object");
		}

	}

	class JlNull extends JlReference {

		public JlNull() {
			super("null");
		}

		@Override
		public JlReference asRef() {
			return this;
		}

		@Override
		public JlObject asObject() {
			throw new DynamicError("Null pointer dereferenced",
					DynamicError.NULL_POINTER);
		}

		@Override
		public JlValue field(VariableSymbol name) {
			throw new DynamicError("Null pointer dereferenced",
					DynamicError.NULL_POINTER);
		}

		@Override
		public void setField(VariableSymbol name, JlValue value) {
			throw new DynamicError("Null pointer dereferenced",
					DynamicError.NULL_POINTER);
		}

		@Override
		public JlValue deref(int idx) {
			throw new DynamicError("Null pointer dereferenced",
					DynamicError.NULL_POINTER);
		}

		@Override
		public void setDeref(int idx, JlValue value) {
			throw new DynamicError("Null pointer dereferenced",
					DynamicError.NULL_POINTER);
		}

		@Override
		public boolean canBeCastTo(String typeName) {
			return true;
		}

		@Override
		public String toString() {
			return "null";
		}

	}

	class JlUninitialized extends JlValue {

		public JlUninitialized() {
			super("uninitialized");
		}

		@Override
		public boolean asBoolean() {
			throw new DynamicError("Uninitialized value!",
					DynamicError.UNINITIALIZED_VALUE);
		}

		@Override
		public int asInt() {
			throw new DynamicError("Uninitialized value!",
					DynamicError.UNINITIALIZED_VALUE);
		}

		@Override
		public float asFloat() {
			throw new DynamicError("Uninitialized value!",
					DynamicError.UNINITIALIZED_VALUE);
		}

		@Override
		public JlObject asObject() {
			throw new DynamicError("Uninitialized value!",
					DynamicError.UNINITIALIZED_VALUE);
		}

		@Override
		public JlReference asRef() {
			throw new DynamicError("Uninitialized value!",
					DynamicError.UNINITIALIZED_VALUE);
		}

		@Override
		public String toString() {
			return "(uninit)";
		}

	}

	class JlFloat extends JlValue {

		private final float value;

		private JlFloat(float value) {
			super("float");
			this.value = value;
		}

		@Override
		public boolean equals(Object o) {

			if (o instanceof JlFloat) {
				return value == ((JlFloat) o).value;
			}

			return false;
		}

		@Override
		public JlValue times(JlValue obj) {

			if (obj instanceof JlFloat) {
				return new JlFloat(this.value * ((JlFloat) obj).value);
			}

			throw new DynamicError("Invalid type!", DynamicError.INTERNAL_ERROR);

		}

		@Override
		public JlValue div(JlValue obj) {

			if (obj instanceof JlFloat) {
				return new JlFloat(this.value / ((JlFloat) obj).value);
			}

			throw new DynamicError("Invalid type!", DynamicError.INTERNAL_ERROR);

		}

		@Override
		public JlValue mod(JlValue obj) {

			if (obj instanceof JlFloat) {
				return new JlFloat(this.value % ((JlFloat) obj).value);
			}

			throw new DynamicError("Invalid type!", DynamicError.INTERNAL_ERROR);

		}

		@Override
		public JlValue add(JlValue obj) {

			if (obj instanceof JlFloat) {
				return new JlFloat(this.value + ((JlFloat) obj).value);
			}

			throw new DynamicError("Invalid type!", DynamicError.INTERNAL_ERROR);

		}

		@Override
		public JlValue subtract(JlValue obj) {

			if (obj instanceof JlFloat) {
				return new JlFloat(this.value - ((JlFloat) obj).value);
			}

			throw new DynamicError("Invalid type!", DynamicError.INTERNAL_ERROR);

		}

		@Override
		public JlValue less(JlValue obj) {

			if (obj instanceof JlFloat) {
				return new JlBoolean(this.value < ((JlFloat) obj).value);
			}

			throw new DynamicError("Invalid type!", DynamicError.INTERNAL_ERROR);

		}

		@Override
		public JlValue lessOrEqual(JlValue obj) {

			if (obj instanceof JlFloat) {
				return new JlBoolean(this.value <= ((JlFloat) obj).value);
			}

			throw new DynamicError("Invalid type!", DynamicError.INTERNAL_ERROR);

		}

		@Override
		public JlValue greater(JlValue obj) {

			if (obj instanceof JlFloat) {
				return new JlBoolean(this.value > ((JlFloat) obj).value);
			}

			throw new DynamicError("Invalid type!", DynamicError.INTERNAL_ERROR);

		}

		@Override
		public JlValue greaterOrEqual(JlValue obj) {

			if (obj instanceof JlFloat) {
				return new JlBoolean(this.value >= ((JlFloat) obj).value);
			}

			throw new DynamicError("Invalid type!", DynamicError.INTERNAL_ERROR);

		}

		@Override
		public JlValue plus() {
			return new JlFloat(value);
		}

		@Override
		public JlValue minus() {
			return new JlFloat(-value);
		}

		@Override
		public float asFloat() {
			return value;
		}

		@Override
		public String toString() {
			return Float.toString(value);
		}

	}

	class JlInt extends JlValue {

		private final int value;

		private JlInt(int value) {
			super("int");
			this.value = value;
		}

		@Override
		public boolean equals(Object o) {

			if (o instanceof JlInt) {
				return value == ((JlInt) o).value;
			}

			return false;
		}

		@Override
		public JlValue times(JlValue obj) {

			if (obj instanceof JlInt) {
				return new JlInt(this.value * ((JlInt) obj).value);
			}

			throw new DynamicError("Invalid type!", DynamicError.INTERNAL_ERROR);

		}

		@Override
		public JlValue div(JlValue obj) {

			if (obj instanceof JlInt) {
				return new JlInt(this.value / ((JlInt) obj).value);
			}

			throw new DynamicError("Invalid type!", DynamicError.INTERNAL_ERROR);

		}

		@Override
		public JlValue mod(JlValue obj) {

			if (obj instanceof JlInt) {
				return new JlInt(this.value % ((JlInt) obj).value);
			}

			throw new DynamicError("Invalid type!", DynamicError.INTERNAL_ERROR);

		}

		@Override
		public JlValue add(JlValue obj) {

			if (obj instanceof JlInt) {
				return new JlInt(this.value + ((JlInt) obj).value);
			}

			throw new DynamicError("Invalid type!", DynamicError.INTERNAL_ERROR);

		}

		@Override
		public JlValue subtract(JlValue obj) {

			if (obj instanceof JlInt) {
				return new JlInt(this.value - ((JlInt) obj).value);
			}

			throw new DynamicError("Invalid type!", DynamicError.INTERNAL_ERROR);

		}

		@Override
		public JlValue less(JlValue obj) {

			if (obj instanceof JlInt) {
				return new JlBoolean(this.value < ((JlInt) obj).value);
			}

			throw new DynamicError("Invalid type!", DynamicError.INTERNAL_ERROR);

		}

		@Override
		public JlValue lessOrEqual(JlValue obj) {

			if (obj instanceof JlInt) {
				return new JlBoolean(this.value <= ((JlInt) obj).value);
			}

			throw new DynamicError("Invalid type!", DynamicError.INTERNAL_ERROR);

		}

		@Override
		public JlValue greater(JlValue obj) {

			if (obj instanceof JlInt) {
				return new JlBoolean(this.value > ((JlInt) obj).value);
			}

			throw new DynamicError("Invalid type!", DynamicError.INTERNAL_ERROR);

		}

		@Override
		public JlValue greaterOrEqual(JlValue obj) {

			if (obj instanceof JlInt) {
				return new JlBoolean(this.value >= ((JlInt) obj).value);
			}

			throw new DynamicError("Invalid type!", DynamicError.INTERNAL_ERROR);

		}

		@Override
		public JlValue plus() {
			return new JlInt(value);
		}

		@Override
		public JlValue minus() {
			return new JlInt(-value);
		}

		@Override
		public int asInt() {
			return value;
		}

		@Override
		public String toString() {
			return Integer.toString(value);
		}

	}

	class JlBoolean extends JlValue {

		private final boolean value;

		private JlBoolean(boolean value) {
			super("boolean");
			this.value = value;
		}

		@Override
		public boolean equals(Object o) {

			if (o instanceof JlBoolean) {
				return value == ((JlBoolean) o).value;
			}

			return false;

		}

		@Override
		public JlValue not() {
			return new JlBoolean(!value);
		}

		@Override
		public JlValue or(JlValue obj) {

			if (obj instanceof JlBoolean) {
				return new JlBoolean(this.value || ((JlBoolean) obj).value);
			}

			throw new DynamicError("Invalid type!", DynamicError.INTERNAL_ERROR);

		}

		@Override
		public JlValue and(JlValue obj) {

			if (obj instanceof JlBoolean) {
				return new JlBoolean(this.value && ((JlBoolean) obj).value);
			}

			throw new DynamicError("Invalid type!", DynamicError.INTERNAL_ERROR);

		}

		@Override
		public boolean asBoolean() {
			return value;
		}

		@Override
		public String toString() {
			return Boolean.toString(value);
		}

	}

	// The stack:
	class StackFrame {

		private final JlObject thisPointer;
		private final Map<VariableSymbol, JlValue> variables;

		public StackFrame(JlObject thisPointer) {
			this.thisPointer = thisPointer;
			this.variables = new HashMap<>();
		}

		public JlValue var(VariableSymbol name) {

			if (variables.containsKey(name)) {
				return variables.get(name);
			}

			return new JlUninitialized();

		}

		public void setVar(VariableSymbol name, JlValue val) {
			variables.put(name, val);
		}

		@Override
		public String toString() {
			return String.format("StackFrame(%s) {%s}",
					System.identityHashCode(this), variables.toString());
		}

		public JlObject getThisPointer() {
			return thisPointer;
		}

	}

	public void step() {

		// Stop after taking too many evaluation steps!
		if (++steps > MAX_STEPS) {
			throw new DynamicError("Uninitialized value!",
					DynamicError.INFINITE_LOOP);
		}

	}

	// The interpreter proper:
	class ExprInterp extends AstVisitor<JlValue, StackFrame> {

		private JlValue v(int value) {
			return new JlInt(value);
		}

		private JlValue v(float value) {
			return new JlFloat(value);
		}

		private JlValue v(boolean b) {
			return new JlBoolean(b);
		}

		@Override
		public JlValue visit(Ast ast, StackFrame arg) {
			step();
			return super.visit(ast, arg);
		}

		@Override
		public JlValue binaryOp(BinaryOp ast, StackFrame arg) {

			try {

				final JlValue left = visit(ast.left(), arg);
				final JlValue right = visit(ast.right(), arg);

				// TODO Only increment this operator for integers
				increment(ast.operator);
				switch (ast.operator) {
				case B_TIMES:
					return left.times(right);
				case B_DIV:
					return left.div(right);
				case B_MOD:
					return left.mod(right);
				case B_PLUS:
					return left.add(right);
				case B_MINUS:
					return left.subtract(right);
				case B_AND:
					return left.and(right);
				case B_OR:
					return left.or(right);
				case B_EQUAL:
					return v(left.equals(right));
				case B_NOT_EQUAL:
					return v(!left.equals(right));
				case B_LESS_THAN:
					return left.less(right);
				case B_LESS_OR_EQUAL:
					return left.lessOrEqual(right);
				case B_GREATER_THAN:
					return left.greater(right);
				case B_GREATER_OR_EQUAL:
					return left.greaterOrEqual(right);
				}

				throw new DynamicError("Unhandled binary operator",
						DynamicError.INTERNAL_ERROR);

			} catch (ArithmeticException e) {
				throw new DynamicError("Division by zero",
						DynamicError.DIVISION_BY_ZERO);
			}
		}

		@Override
		public JlValue booleanConst(BooleanConst ast, StackFrame arg) {
			return v(ast.value);
		}

		@Override
		public JlValue builtInRead(BuiltInRead ast, StackFrame arg) {
			return v(input.nextInt());
		}

		@Override
		public JlValue builtInReadFloat(BuiltInReadFloat ast, StackFrame arg) {
			return v(input.nextFloat());
		}

		@Override
		public JlValue cast(Cast ast, StackFrame arg) {

			JlReference ref = visit(ast.arg(), arg).asRef();

			if (ref.canBeCastTo(ast.typeName)) {
				return ref;
			}

			throw new DynamicError("Cast failure: cannot cast " + ref.typeName
					+ " to " + ast.typeName, DynamicError.INVALID_DOWNCAST);

		}

		@Override
		public JlValue field(Field ast, StackFrame arg) {
			JlValue lhs = visit(ast.arg(), arg);
			return lhs.asRef().field(ast.sym);
		}

		@Override
		public JlValue index(Index ast, StackFrame arg) {
			JlValue lhs = visit(ast.left(), arg);
			JlValue idx = visit(ast.right(), arg);
			return lhs.asRef().deref(idx.asInt());
		}

		@Override
		public JlValue intConst(IntConst ast, StackFrame arg) {
			return v(ast.value);
		}

		@Override
		public JlValue floatConst(FloatConst ast, StackFrame arg) {
			return v(ast.value);
		}

		@Override
		public JlValue newArray(NewArray ast, StackFrame arg) {
			JlValue size = visit(ast.arg(), arg);
			return new JlArray(ast.typeName, size.asInt());
		}

		@Override
		public JlValue newObject(NewObject ast, StackFrame arg) {
			return new JlObject(ast.typeName);
		}

		@Override
		public JlValue nullConst(NullConst ast, StackFrame arg) {
			return nullPointer;
		}

		@Override
		public JlValue thisRef(ThisRef ast, StackFrame arg) {
			return arg.getThisPointer();
		}

		@Override
		public JlValue methodCall(MethodCallExpr ast, StackFrame frame) {

			JlObject rcvr = expr(ast.receiver(), frame).asObject();

			List<JlValue> arguments = new ArrayList<>();
			for (Ast arg : ast.argumentsWithoutReceiver()) {
				arguments.add(expr(arg, frame));
			}

			return invokeMethod(rcvr.typeName, ast.methodName, rcvr, arguments);

		}

		@Override
		public JlValue unaryOp(UnaryOp ast, StackFrame arg) {

			JlValue val = visit(ast.arg(), arg);

			// TODO Increment this only when is an int, we don't optimize floats
			increment(ast.operator);

			switch (ast.operator) {
			case U_PLUS:
				return val.plus();
			case U_MINUS:
				return val.minus();
			case U_BOOL_NOT:
				return val.not();
			}

			throw new DynamicError("Unhandled unary operator " + ast.operator,
					DynamicError.INTERNAL_ERROR);
		}

		@Override
		public JlValue var(Var ast, StackFrame arg) {

			if (ast.sym == null) {
				throw new DynamicError("Var node with null symbol",
						DynamicError.INTERNAL_ERROR);
			}

			switch (ast.sym.getKind()) {

			case LOCAL:
			case PARAM:
				return arg.var(ast.sym);
			case FIELD:
				return arg.getThisPointer().field(ast.sym);
			}

			throw new DynamicError("Unhandled VariableSymbol kind: "
					+ ast.sym.getKind(), DynamicError.INTERNAL_ERROR);

		}

	}

	public JlValue expr(Ast ast, StackFrame frame) {
		return new ExprInterp().visit(ast, frame);
	}

	public ClassDecl findClassDecl(String typeName) {

		for (ClassDecl cd : classDecls) {

			if (cd.name.equals(typeName)) {
				return cd;
			}

		}

		throw new StaticError("No such type " + typeName);

	}

	class MethodInterp extends AstVisitor<JlValue, StackFrame> {

		@Override
		public JlValue assign(Assign ast, final StackFrame frame) {

			final JlValue val = expr(ast.right(), frame);

			new AstVisitor<Void, Void>() {

				@Override
				public Void field(Field ast, Void arg) {
					JlValue obj = expr(ast.arg(), frame);
					assert obj != null && obj.asRef() != null;
					obj.asRef().setField(ast.sym, val);
					return null;
				}

				@Override
				public Void index(Index ast, Void arg) {
					JlValue obj = expr(ast.left(), frame);
					JlValue idx = expr(ast.right(), frame);
					obj.asRef().setDeref(idx.asInt(), val);
					return null;
				}

				@Override
				public Void var(Var ast, Void arg) {
					frame.setVar(ast.sym, val);
					return null;
				}

				@Override
				protected Void dflt(Ast ast, Void arg) {
					throw new StaticError("Malformed l-value in AST");
				}

			}.visit(ast.left(), null);

			return null;
		}

		@Override
		public JlValue builtInWrite(BuiltInWrite ast, StackFrame frame) {

			JlValue val = expr(ast.arg(), frame);

			try {
				output.write(Integer.toString(val.asInt()));
			} catch (IOException e) {
				throw new DynamicError(e, DynamicError.INTERNAL_ERROR);
			}

			return null;

		}

		@Override
		public JlValue builtInWriteFloat(BuiltInWriteFloat ast, StackFrame frame) {

			JlValue val = expr(ast.arg(), frame);

			try {
				output.write(String.format("%.5f", val.asFloat()));
			} catch (IOException e) {
				throw new DynamicError(e, DynamicError.INTERNAL_ERROR);
			}

			return null;
		}

		@Override
		public JlValue builtInWriteln(BuiltInWriteln ast, StackFrame arg) {

			try {
				output.write("\n");
			} catch (IOException e) {
				throw new DynamicError(e, DynamicError.INTERNAL_ERROR);
			}

			return null;
		}

		@Override
		public JlValue ifElse(IfElse ast, StackFrame frame) {

			JlValue cond = expr(ast.condition(), frame);

			if (cond.asBoolean()) {
				return visit(ast.then(), frame);
			} else {
				return visit(ast.otherwise(), frame);
			}

		}

		@Override
		public JlValue methodCall(MethodCall ast, final StackFrame frame) {

			JlObject rcvr = expr(ast.receiver(), frame).asObject();

			List<JlValue> arguments = new ArrayList<>();
			for (Ast arg : ast.argumentsWithoutReceiver()) {
				arguments.add(expr(arg, frame));
			}

			return invokeMethod(rcvr.typeName, ast.methodName, rcvr, arguments);

		}

		@Override
		public JlValue whileLoop(WhileLoop ast, StackFrame frame) {

			while (true) {

				JlValue cond = expr(ast.condition(), frame);

				if (!cond.asBoolean()) {
					return null;
				}

				visit(ast.body(), frame);
			}

		}

		@Override
		public JlValue returnStmt(ReturnStmt ast, StackFrame frame) {

			if (ast.arg() != null) {
				return expr(ast.arg(), frame);
			}

			return new JlNull();

		}

	}

	public MethodDecl findMethodDecl(String origTypeName,
			final String methodName) {

		String typeName = origTypeName;

		while (!typeName.equals("Object")) {

			ClassDecl cd = findClassDecl(typeName);

			final List<MethodDecl> result = new ArrayList<>();

			for (Ast mem : cd.members()) {

				AstVisitor<Void, Void> vis = new AstVisitor<Void, Void>() {
					@Override
					public Void methodDecl(MethodDecl ast, Void arg) {

						if (!ast.name.equals(methodName)) {
							return null;
						}
						result.add(ast);

						return null;
					}
				};

				vis.visit(mem, null);
			}

			if (result.size() == 1) {
				return result.get(0);
			}

			if (result.size() > 1) {
				throw new StaticError(result.size()
						+ " implementations of method " + methodName
						+ " found in type " + typeName);
			}

			typeName = cd.superClass;
		}

		throw new StaticError("No method " + methodName + " in type "
				+ origTypeName);

	}

	// Note: does not interpret phis!
	public JlValue interpretCfg(ControlFlowGraph cfg, final StackFrame frame) {

		BasicBlock current = cfg.start;
		MethodInterp minterp = new MethodInterp();
		JlValue res = null;

		while (true) {

			step();

			for (Ast instruction : current.instructions) {

				res = minterp.visit(instruction, frame);

				if (instruction instanceof ReturnStmt) {
					return res;
				}

			}

			if (current == cfg.end) {
				return res;
			} else if (current.condition == null) {
				current = current.successors.get(0);
			} else {

				JlValue cond = expr(current.condition, frame);

				if (cond.asBoolean()) {
					current = current.trueSuccessor();
				} else {
					current = current.falseSuccessor();
				}

			}

		}

	}

	public JlValue invokeMethod(final String typeName, final String methodName,
			final JlObject rcvr, final List<JlValue> arguments) {

		MethodDecl mdecl = findMethodDecl(typeName, methodName);
		StackFrame newFrame = new StackFrame(rcvr);
		int idx = 0;

		for (VariableSymbol sym : mdecl.sym.getParameters()) {
			newFrame.setVar(sym, arguments.get(idx++));
		}

		if (mdecl.cfg != null) {
			return interpretCfg(mdecl.cfg, newFrame);
		} else {
			return new MethodInterp().visit(mdecl.body(), newFrame);
		}

	}
}
