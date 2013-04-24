package cd.codegen;

import static cd.Config.MAIN;
import static cd.Config.PRINTF;
import static cd.Config.SCANF;
import static cd.Config.SIZEOF_PTR;
import static java.lang.Math.max;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import cd.Config;
import cd.Main;
import cd.debug.AstOneLine;
import cd.ir.Ast;
import cd.ir.Ast.Assign;
import cd.ir.Ast.BinaryOp;
import cd.ir.Ast.BinaryOp.BOp;
import cd.ir.Ast.BooleanConst;
import cd.ir.Ast.BuiltInRead;
import cd.ir.Ast.BuiltInReadFloat;
import cd.ir.Ast.BuiltInWrite;
import cd.ir.Ast.BuiltInWriteFloat;
import cd.ir.Ast.BuiltInWriteln;
import cd.ir.Ast.Cast;
import cd.ir.Ast.ClassDecl;
import cd.ir.Ast.Expr;
import cd.ir.Ast.Field;
import cd.ir.Ast.FloatConst;
import cd.ir.Ast.IfElse;
import cd.ir.Ast.Index;
import cd.ir.Ast.IntConst;
import cd.ir.Ast.MethodCall;
import cd.ir.Ast.MethodCallExpr;
import cd.ir.Ast.MethodDecl;
import cd.ir.Ast.NewArray;
import cd.ir.Ast.NewObject;
import cd.ir.Ast.NullConst;
import cd.ir.Ast.ReturnStmt;
import cd.ir.Ast.ThisRef;
import cd.ir.Ast.UnaryOp;
import cd.ir.Ast.Var;
import cd.ir.Ast.WhileLoop;
import cd.ir.AstVisitor;
import cd.ir.ExprVisitor;
import cd.ir.Symbol.ArrayTypeSymbol;
import cd.ir.Symbol.ClassSymbol;
import cd.ir.Symbol.MethodSymbol;
import cd.ir.Symbol.TypeSymbol;
import cd.ir.Symbol.VariableSymbol;
import cd.util.Pair;

public class AstCodeGenerator {

	protected final RegsNeededVisitor rnv = new RegsNeededVisitor();
	protected final ExprGenerator eg = new ExprGenerator();
	protected final StmtDeclGenerator sdg = new StmtDeclGenerator();
	protected final Writer out;
	protected final Main main;

	public AstCodeGenerator(Main main, Writer out) {
		initRegisters();
		this.out = out;
		this.main = main;
	}

	public void debug(String format, Object... args) {
		this.main.debug(format, args);
	}

	/**
	 * The address of the this ptr relative to the BP. Note that the this ptr is
	 * always the first argument.
	 */
	protected static final int THIS_OFFSET = 8;

	/**
	 * Name of the internal checkCast() helper function we generate.
	 */
	private static final String CHECK_CAST = "checkCast";

	/**
	 * Name of the internal checkNull() helper function we generate.
	 */
	private static final String CHECK_NULL = "checkNull";

	/**
	 * Name of the internal checkNonZero() helper function we generate.
	 */
	private static final String CHECK_NON_ZERO = "checkNonZero";

	private static final String CHECK_ARRAY_SIZE = "checkArraySize";

	/**
	 * Main method. Causes us to emit x86 assembly corresponding to {@code ast}
	 * into {@code file}. Throws a {@link RuntimeException} should any I/O error
	 * occur.
	 * 
	 * <p>
	 * The generated file will be divided into three sections:
	 * <ol>
	 * <li>Prologue: Generated by {@link #emitPrologue()}. This contains any
	 * introductory declarations and the like.
	 * <li>Body: Generated by {@link ExprGenerator}. This contains the main
	 * method definitions.
	 * <li>Epilogue: Generated by {@link #emitEpilogue()}. This contains any
	 * final declarations required.
	 * </ol>
	 */
	public void go(List<? extends ClassDecl> astRoots) {

		emitPrefix(astRoots);
		for (ClassDecl ast : astRoots) {
			sdg.gen(ast);
		}
	}

	public void emitPrefix(List<? extends ClassDecl> astRoots) {
		// compute method and field offsets
		for (ClassDecl ast : astRoots) {
			computeFieldOffsets(ast.sym);
			computeVtableOffsets(ast.sym);
		}

		// emit vtables
		for (TypeSymbol ts : main.allTypeSymbols)
			emitVtable(ts);

		// Emit some useful string constants and static data:
		emit(Config.DATA_STR_SECTION);
		emitLabel("STR_NL");
		emit(Config.DOT_STRING + " \"\\n\"");
		emitLabel("STR_D");
		emit(Config.DOT_STRING + " \"%d\"");
		emitLabel("STR_F");
		emit(Config.DOT_STRING + " \"%.10f\"");
		emitLabel("SCANF_STR_F");
		emit(Config.DOT_STRING + " \"%f\"");
		emit(Config.DATA_INT_SECTION);

		emit(Config.TEXT_SECTION);

		String obj = callerSave[0];
		String cls = callerSave[1];
		String looplbl = uniqueLabel();
		String donelbl = uniqueLabel();
		String faillbl = uniqueLabel();
		emitCommentSection("checkCast() function");
		emitLabel(CHECK_CAST);
		emit("enter", "$8", "$0");
		emitLoad(SIZEOF_PTR * 2, BP, cls);
		emitLoad(SIZEOF_PTR * 3, BP, obj);
		emit("cmpl", "$0", obj);
		emit("je", donelbl); // allow null objects to pass
		emitLoad(0, obj, obj); // load vtbl of object
		emitLabel(looplbl);
		emit("cmpl", obj, cls);
		emit("je", donelbl);
		emit("cmpl", "$0", obj);
		emit("je", faillbl);
		emitLoad(0, obj, obj); // load parent vtable
		emit("jmp", looplbl);
		emitLabel(faillbl);
		emitStore(c(1), 0, SP);
		emit("call", Config.EXIT);
		emitLabel(donelbl);
		emit("leave");
		emit("ret");
		String oknulllbl = uniqueLabel();
		emitCommentSection("checkNull() function");
		emitLabel(CHECK_NULL);
		emit("enter", "$8", "$0");
		emit("cmpl", "$0", o(SIZEOF_PTR * 2, BP));
		emit("jne", oknulllbl);
		emitStore(c(4), 0, SP);
		emit("call", Config.EXIT);
		emitLabel(oknulllbl);
		emit("leave");
		emit("ret");
		String oknzlbl = uniqueLabel();
		emitCommentSection("checkNonZero() function");
		emitLabel(CHECK_NON_ZERO);
		emit("enter", "$8", "$0");
		emit("cmpl", "$0", o(SIZEOF_PTR * 2, BP));
		emit("jne", oknzlbl);
		emitStore(c(8), 0, SP);
		emit("call", Config.EXIT);
		emitLabel(oknzlbl);
		emit("leave");
		emit("ret");
		String okunqlbl = uniqueLabel();
		emitCommentSection("checkArraySize() function");
		emitLabel(CHECK_ARRAY_SIZE);
		emit("enter", "$8", "$0");
		emit("cmpl", "$0", o(SIZEOF_PTR * 2, BP));
		emit("jge", okunqlbl);
		emitStore(c(5), 0, SP);
		emit("call", Config.EXIT);
		emitLabel(okunqlbl);
		emit("leave");
		emit("ret");
		// Generate AST for main() method:
		// new Main().main()
		Ast.NewObject newMain = new Ast.NewObject("Main");
		newMain.type = main.mainType;
		Ast.MethodCall callMain = new Ast.MethodCall(newMain, "main",
				Collections.<Expr> emptyList());
		callMain.sym = main.mainType.getMethod("main");

		// Emit the main() method:
		// new Main().main();
		emitCommentSection("main() function");
		emit(".globl " + MAIN);
		emitLabel(MAIN);
		emit("enter", "$8", "$0");
		sdg.gen(callMain);
		emit("movl", "$0", "%eax"); // normal termination:
		emit("leave");
		emit("ret");
	}

	/**
	 * Computes the vtable offset for each method defined in the class
	 * {@code sym}.
	 */
	private int computeVtableOffsets(ClassSymbol sym) {

		if (sym == null)
			return 0;

		if (sym.totalMethods != -1)
			return sym.totalMethods;

		int index = computeVtableOffsets(sym.superClass);
		for (MethodSymbol ms : sym.methods.values()) {
			assert ms.vtableIndex == -1;
			if (ms.overrides != null)
				ms.vtableIndex = ms.overrides.vtableIndex;
			else
				ms.vtableIndex = index++;
		}
		sym.totalMethods = index;
		return index;
	}

	/**
	 * Computes the offset for each field.
	 */
	private int computeFieldOffsets(ClassSymbol sym) {
		if (sym == null)
			return 0;

		if (sym.totalFields != -1)
			return sym.totalFields;

		int index = computeFieldOffsets(sym.superClass);
		for (VariableSymbol fs : sym.fields.values()) {
			assert fs.offset == -1;
			// compute offset in bytes; note that 0 is the vtable
			fs.offset = (index * SIZEOF_PTR) + SIZEOF_PTR;
			index++;
		}
		sym.totalFields = index;
		sym.sizeof = (sym.totalFields + 1) * Config.SIZEOF_PTR;
		return index;
	}

	private void collectVtable(MethodSymbol[] vtable, ClassSymbol sym) {
		if (sym.superClass != null)
			collectVtable(vtable, sym.superClass);
		for (MethodSymbol ms : sym.methods.values())
			vtable[ms.vtableIndex] = ms;
	}

	private void emitVtable(TypeSymbol ts) {
		if (ts instanceof ClassSymbol) {
			ClassSymbol cs = (ClassSymbol) ts;

			// Collect the vtable:
			MethodSymbol[] vtable = new MethodSymbol[cs.totalMethods];
			collectVtable(vtable, cs);

			// Emit vtable for this class:
			emitLabel(vtable(cs));
			if (cs.superClass != null)
				emitConstantData(vtable(cs.superClass));
			else
				emitConstantData("0");
			for (int i = 0; i < cs.totalMethods; i++)
				emitConstantData(mthdlbl(vtable[i]));
		} else if (ts instanceof ArrayTypeSymbol) {
			ArrayTypeSymbol as = (ArrayTypeSymbol) ts;
			emitLabel(vtable(as));
			emitConstantData(vtable(main.objectType));
		}
	}

	private String vtable(TypeSymbol ts) {
		if (ts instanceof ClassSymbol) {
			return "vtable_" + ((ClassSymbol) ts).name;
		} else if (ts instanceof ArrayTypeSymbol) {
			return "vtablearr_" + ((ArrayTypeSymbol) ts).elementType.name;
		} else {
			throw new RuntimeException("No vtable for " + ts.name);
		}
	}

	// Simplistic Register management:
	List<String> registers = new ArrayList<String>();

	final String calleeSave[] = new String[] { "%esi", "%edi", "%ebx" };

	final String callerSave[] = new String[] { "%eax", "%ecx", "%edx" };

	private static final String FLOAT_REG_1 = "%xmm1";

	private static final String FLOAT_REG_0 = "%xmm0";

	final String BP = "%ebp", SP = "%esp";

	final int SIZEOF_REG = 4;

	protected void initRegisters() {
		registers.add("%eax");
		registers.add("%ebx");
		registers.add("%ecx");
		registers.add("%edx");
		registers.add("%esi");
		registers.add("%edi");
	}

	protected String getRegister() {
		int last = registers.size() - 1;
		assert last >= 0 : "Program requires too many registers";
		return registers.remove(last);
	}

	protected void releaseRegister(String reg) {
		assert !registers.contains(reg);
		registers.add(reg);
	}

	protected boolean isInUse(String reg) {
		return !registers.contains(reg);
	}

	protected int availableRegisters() {
		return registers.size();
	}

	/** Returns true for %e[abcd]x */
	protected boolean hasLowByteVersion(String reg) {
		return reg.startsWith("%e") && reg.endsWith("x");
	}

	/**
	 * Used to track the current number of temporaries in use. Because we do not
	 * grow our stack dynamically, we allocate a set amount of space for
	 * "temporary" values. Values can be stored and retrieved using
	 * {@link #push(String)} and {@link #pop(String)}, which use the current
	 * slot stored in {@code tempOffset} as an address to load and store data
	 * from.
	 */
	protected int tempOffset;

	/** @see #tempOffset */
	protected void push(String reg) {
		emitStore(reg, tempOffset, BP);
		tempOffset -= Config.SIZEOF_PTR;
	}

	/** @see #tempOffset */
	protected void pop(String reg) {
		tempOffset += Config.SIZEOF_PTR;
		emitLoad(tempOffset, BP, reg);
	}

	protected void restoreCalleeSaveRegs() {
		int offset = 0;
		for (int reg = calleeSave.length - 1; reg >= 0; reg--) {
			// don't use pop here to not change tempOffset at each return stmt.
			offset += Config.SIZEOF_PTR;
			emitLoad(tempOffset + offset, BP, calleeSave[reg]);
		}
	}

	protected void storeCallSaveRegs() {
		for (int reg = 0; reg < calleeSave.length; reg++) {
			push(calleeSave[reg]);
		}
	}

	protected void call(String target, String res) {
		for (int reg = 0; reg < callerSave.length; reg++) {
			if (registers.contains(callerSave[reg]))
				continue; // not in use
			if (callerSave[reg].equals(res))
				continue; // will contain our result
			push(callerSave[reg]);
		}
		// Emit the call and save the return value:
		emit("call", target);
		if (res != null)
			emitMove("%eax", res);

		for (int reg = callerSave.length - 1; reg >= 0; reg--) {
			if (registers.contains(callerSave[reg]))
				continue; // not in use
			if (callerSave[reg].equals(res))
				continue; // contains our result
			pop(callerSave[reg]);
		}
	}

	/**
	 * Given a register like {@code %eax} returns {@code %al}, but doesn't work
	 * for {@code %esi} and {@code %edi}!
	 */
	protected String lowByteVersion(String reg) {
		assert hasLowByteVersion(reg);
		return "%" + reg.charAt(2) + "l";
	}

	/**
	 * Generates code which evaluates {@code ast} and branches to {@code lbl} if
	 * the value generated for {@code ast} is false.
	 */
	public void genJumpIfFalse(Expr ast, String lbl) {
		// A better way to implement this would be with a separate
		// visitor.
		String reg = eg.gen(ast);
		emit("cmpl", "$0", reg);
		emit("je", lbl);
		releaseRegister(reg);
	}

	/**
	 * Generates code to evaluate expressions. After emitting the code, returns
	 * a String which indicates the register where the result can be found.
	 */
	protected class ExprGenerator extends ExprVisitor<String, Void> {

		public String gen(Expr ast) {
			return visit(ast, null);
		}

		/**
		 * This routine handles register shortages. It generates a value for
		 * {@code right}, while keeping the value in {@code leftReg} live.
		 * However, if there are insufficient registers, it may temporarily
		 * store the value in {@code leftReg} to the stack. In this case, it
		 * will be restored into another register once {@code right} has been
		 * evaluated, but the register may not be the same as {@code leftReg}.
		 * Therefore, this function returns a pair of registers, the first of
		 * which stores the left value, and the second of which stores the right
		 * value.
		 * 
		 * Note that we don't actually use the push instructions, instead we use
		 * our temporary data area. (See visitor for method decls for details)
		 */
		public Pair<String> genPushing(String leftReg, Expr right) {

			boolean pop = false;

			if (rnv.calc(right) > availableRegisters()) {
				push(leftReg);
				releaseRegister(leftReg);
				pop = true;
			}

			String rightReg = gen(right);

			if (pop) {
				leftReg = getRegister();
				pop(leftReg);
			}

			return new Pair<String>(leftReg, rightReg);

		}

		@Override
		public String visit(Expr ast, Void arg) {

			try {
				emitIndent("Emitting " + AstOneLine.toString(ast));
				return super.visit(ast, null);
			} finally {
				emitUndent();
			}

		}

		private abstract class OperandsDispatcher {

			public abstract void integerOp(String leftReg, Ast.BinaryOp.BOp op,
					String rightReg);

			public abstract void floatOp(String leftReg, Ast.BinaryOp.BOp op,
					String rightReg);

			public abstract void floatCmp(String leftReg, Ast.BinaryOp.BOp op,
					String rightReg);

			public void binaryOp(Ast.BinaryOp ast, String leftReg,
					String rightReg) {

				if (ast.type == main.floatType) {
					floatOp(leftReg, ast.operator, rightReg);
				} else {

					if (ast.left().type == main.floatType) {
						floatCmp(leftReg, ast.operator, rightReg);
					} else {
						integerOp(leftReg, ast.operator, rightReg);
					}

				}

			}

		}

		@Override
		public String binaryOp(BinaryOp ast, Void arg) {
			int leftRN = rnv.calc(ast.left());
			int rightRN = rnv.calc(ast.right());
			String leftReg, rightReg;
			if (leftRN > rightRN) {
				leftReg = gen(ast.left());
				Pair<String> regs = genPushing(leftReg, ast.right());
				leftReg = regs.a;
				rightReg = regs.b;
			} else {
				rightReg = gen(ast.right());
				Pair<String> regs = genPushing(rightReg, ast.left());
				rightReg = regs.a;
				leftReg = regs.b;
			}
			new OperandsDispatcher() {

				@Override
				public void integerOp(String leftReg, BOp op, String rightReg) {

					switch (op) {
					case B_TIMES:
						emit("imull", rightReg, leftReg);
						break;
					case B_PLUS:
						emit("addl", rightReg, leftReg);
						break;
					case B_MINUS:
						emit("subl", rightReg, leftReg);
						break;
					case B_DIV:
						emitDivMod("%eax", leftReg, rightReg);
						break;
					case B_MOD:
						emitDivMod("%edx", leftReg, rightReg);
						break;
					case B_AND:
						emit("andl", rightReg, leftReg);
						break;
					case B_OR:
						emit("orl", rightReg, leftReg);
						break;
					case B_EQUAL:
						emitCmp("sete", leftReg, rightReg);
						break;
					case B_NOT_EQUAL:
						emitCmp("setne", leftReg, rightReg);
						break;
					case B_LESS_THAN:
						emitCmp("setl", leftReg, rightReg);
						break;
					case B_LESS_OR_EQUAL:
						emitCmp("setle", leftReg, rightReg);
						break;
					case B_GREATER_THAN:
						emitCmp("setg", leftReg, rightReg);
						break;
					case B_GREATER_OR_EQUAL:
						emitCmp("setge", leftReg, rightReg);
						break;
					}

				}

				@Override
				public void floatOp(String leftReg, BOp op, String rightReg) {

					emitGprRegToFloatReg(FLOAT_REG_0, leftReg);
					emitGprRegToFloatReg(FLOAT_REG_1, rightReg);

					switch (op) {
					case B_TIMES:
						emit("mulss", FLOAT_REG_1, FLOAT_REG_0);
						break;
					case B_PLUS:
						emit("addss", FLOAT_REG_1, FLOAT_REG_0);
						break;
					case B_MINUS:
						emit("subss", FLOAT_REG_1, FLOAT_REG_0);
						break;
					case B_DIV:
						emit("divss", FLOAT_REG_1, FLOAT_REG_0);
						break;
					case B_MOD:
						break; // This operation is not supported
					default:
						break;
					}

					emitFloatRegToGprReg(leftReg, FLOAT_REG_0);

				}

				@Override
				public void floatCmp(String leftReg, BOp op, String rightReg) {

					switch (op) {
					case B_EQUAL:
						emitCmpFloat("je", leftReg, rightReg);
						break;
					case B_NOT_EQUAL:
						emitCmpFloat("jne", leftReg, rightReg);
						break;
					case B_LESS_THAN:
						emitCmpFloat("jb", leftReg, rightReg);
						break;
					case B_LESS_OR_EQUAL:
						emitCmpFloat("jbe", leftReg, rightReg);
						break;
					case B_GREATER_THAN:
						emitCmpFloat("ja", leftReg, rightReg);
						break;
					case B_GREATER_OR_EQUAL:
						emitCmpFloat("jae", leftReg, rightReg);
						break;
					default:
						break;
					}

				}

			}.binaryOp(ast, leftReg, rightReg);
			releaseRegister(rightReg);
			return leftReg;
		}

		private void emitCmp(String opname, String leftReg, String rightReg) {

			emit("cmpl", rightReg, leftReg);

			if (hasLowByteVersion(leftReg)) {
				emit("movl", "$0", leftReg);
				emit(opname, lowByteVersion(leftReg));
			} else {
				push("%eax");
				emit("movl", "$0", "%eax");
				emit(opname, "%al");
				emit("movl", "%eax", leftReg);
				pop("%eax");
			}

		}

		private void emitCmpFloat(String opname, String leftReg, String rightReg) {

			String label = uniqueLabel();
			String trueLabel = "fcmp_" + label + "_true";
			String endLabel = "fcmp_end" + label;

			emitGprRegToFloatReg(FLOAT_REG_0, leftReg);
			emitGprRegToFloatReg(FLOAT_REG_1, rightReg);

			emit("ucomiss", FLOAT_REG_1, FLOAT_REG_0);
			emit(opname, trueLabel);
			emitMove("$0", leftReg);
			emit("jmp", endLabel);
			emitLabel(trueLabel);
			emitMove("$1", leftReg);
			emitLabel(endLabel);

		}

		private void emitDivMod(String whichResultReg, String leftReg,
				String rightReg) {
			// Compare right reg for 0
			emitStore(rightReg, 0, SP);
			call(CHECK_NON_ZERO, null);

			// Save EAX, EBX, and EDX to the stack if they are not used
			// in this subtree (but are used elsewhere). We will be
			// changing them.
			List<String> dontBother = Arrays.asList(rightReg, leftReg);
			String[] affected = { "%eax", "%ebx", "%edx" };
			for (String s : affected)
				if (!dontBother.contains(s) && isInUse(s))
					emit("pushl", s);

			// Move the LHS (numerator) into eax
			// Move the RHS (denominator) into ebx
			emit("pushl", rightReg);
			emit("pushl", leftReg);
			emit("popl", "%eax");
			emit("popl", "%ebx");
			emit("cltd"); // sign-extend %eax into %edx
			emit("idivl", "%ebx"); // division, result into edx:eax

			// Move the result into the LHS, and pop off anything we saved
			emit("movl", whichResultReg, leftReg);
			for (int i = affected.length - 1; i >= 0; i--) {
				String s = affected[i];
				if (!dontBother.contains(s) && isInUse(s))
					emit("popl", s);
			}
		}

		@Override
		public String booleanConst(BooleanConst ast, Void arg) {
			String reg = getRegister();
			emit("movl", ast.value ? "$1" : "$0", reg);
			return reg;
		}

		@Override
		public String builtInRead(BuiltInRead ast, Void arg) {
			// Since scanf wants to write to memory directly, we give
			// it a ptr to a temporary slot on the stack, and then load
			// the result from this into the reg afterwards.
			String reg = getRegister();
			emit("leal", o(8, SP), reg);
			emitStore(reg, 4, SP);
			emitStore("$STR_D", 0, SP);
			call(SCANF, null);
			emitLoad(8, SP, reg);
			return reg;
		}

		@Override
		public String builtInReadFloat(BuiltInReadFloat ast, Void arg) {

			// Since scanf wants to write to memory directly, we give
			// it a ptr to a temporary slot on the stack, and then load
			// the result from this into the reg afterwards.
			String reg = getRegister();
			emit("leal", o(8, SP), reg);
			emitStore(reg, 4, SP);
			emitStore("$SCANF_STR_F", 0, SP);
			call(SCANF, null);
			emitLoad(8, SP, reg);
			return reg;

		}

		@Override
		public String cast(Cast ast, Void arg) {
			// Invoke the helper function. If it does not exit,
			// the cast succeeded!
			String objReg = gen(ast.arg());
			emitStore(objReg, Config.SIZEOF_PTR, SP);
			emitStore(c(vtable(ast.typeSym)), 0, SP);
			call(CHECK_CAST, null);
			return objReg;
		}

		@Override
		public String index(Index ast, Void arg) {
			String arr = gen(ast.left());
			emitStore(arr, 0, SP);
			call(CHECK_NULL, null);
			Pair<String> pair = genPushing(arr, ast.right());
			arr = pair.a;
			String idx = pair.b;
			// String idx = gen(ast.right());
			emitMove(a(arr, idx), idx);
			releaseRegister(arr);
			return idx;
		}

		@Override
		public String intConst(IntConst ast, Void arg) {
			String reg = getRegister();
			emit("movl", "$" + ast.value, reg);
			return reg;
		}

		@Override
		public String floatConst(FloatConst ast, Void arg) {
			String reg = getRegister();
			int floatValue = Float.floatToRawIntBits(ast.value);
			emit("movl", "$" + floatValue, reg);
			return reg;
		}

		@Override
		public String field(Field ast, Void arg) {
			String reg = gen(ast.arg());
			emitStore(reg, 0, SP); // check for null ptr
			call(CHECK_NULL, null);
			emitLoad(ast.sym.offset, reg, reg);
			return reg;
		}

		@Override
		public String newArray(NewArray ast, Void arg) {
			// Size of the array = 4 + elemsize * num elem.
			// Compute that into reg, store it into the stack as
			// an argument to malloc(), and then use it to store final
			// result.
			ArrayTypeSymbol arrsym = (ArrayTypeSymbol) ast.type;
			String reg = gen(ast.arg());
			// Check for negative array sizes
			emitStore(reg, 0, SP);
			call(CHECK_ARRAY_SIZE, null);
			emit("imul", Config.SIZEOF_PTR, reg);
			emit("addl", Config.SIZEOF_PTR, reg);
			emitStore(reg, 0, SP);
			call(Config.MALLOC, reg);
			emitStore(c(vtable(arrsym)), 0, reg);
			return reg;
		}

		@Override
		public String newObject(NewObject ast, Void arg) {
			ClassSymbol clssym = (ClassSymbol) ast.type;
			String reg = getRegister();
			emitStore(c(clssym.sizeof), 0, SP);
			call(Config.MALLOC, reg);
			emitStore(c(vtable(clssym)), 0, reg);
			return reg;
		}

		@Override
		public String nullConst(NullConst ast, Void arg) {
			String reg = getRegister();
			emit("movl", "$0", reg);
			return reg;
		}

		@Override
		public String thisRef(ThisRef ast, Void arg) {
			String reg = getRegister();
			emitLoad(THIS_OFFSET, BP, reg);
			return reg;
		}

		@Override
		public String methodCall(MethodCallExpr ast, Void arg) {

			return sdg.methodCall(ast.sym, ast.allArguments());
		}

		@Override
		public String unaryOp(UnaryOp ast, Void arg) {
			String argReg = gen(ast.arg());
			switch (ast.operator) {
			case U_PLUS:
				break;

			case U_MINUS: {
				if (ast.type == main.floatType) {
					emitGprRegToFloatReg(FLOAT_REG_0, argReg);
					push("%eax");
					emit("movd", FLOAT_REG_0, "%eax");
					emit("xorl", "$2147483648", "%eax");
					emit("movd", "%eax", FLOAT_REG_0);
					pop("%eax");
					emitFloatRegToGprReg(argReg, FLOAT_REG_0);
				} else {
					emit("negl", argReg);
				}
			}
				break;

			case U_BOOL_NOT:
				emit("negl", argReg);
				emit("incl", argReg);
				break;
			}
			return argReg;
		}

		@Override
		public String var(Var ast, Void arg) {
			String reg = getRegister();
			switch (ast.sym.kind) {
			case LOCAL:
			case PARAM:
				emitLoad(ast.sym.offset, BP, reg);
				break;
			case FIELD:
				// These are removed by the ExprRewriter added to the
				// end of semantic analysis.
				throw new RuntimeException("Should not happen");
			}
			return reg;
		}

	}

	/**
	 * Generates code to process statements and declarations.
	 */
	public class StmtDeclGenerator extends AstVisitor<String, Void> {

		public void gen(Ast ast) {
			visit(ast, null);
		}

		@Override
		public String visit(Ast ast, Void arg) {
			try {
				emitIndent("Emitting " + AstOneLine.toString(ast));
				return super.visit(ast, arg);
			} finally {
				emitUndent();
			}
		}

		public String methodCall(MethodSymbol mthSymbol, List<Expr> args) {

			// Push the arguments onto the stack in reverse order.
			// Note that the space for the arguments is already reserved,
			// so we just write them to 0(%esp), -4(%esp) and so on.
			//
			// After each iteration of the following loop, reg holds the
			// register used for the previous argument. When the
			// loop terminates, reg therefore holds the method receiver.
			List<Expr> allArgs = args;
			String reg = null;
			for (int i = allArgs.size() - 1; i >= 0; i--) {
				int offset = i * Config.SIZEOF_PTR;
				if (reg != null)
					releaseRegister(reg);
				reg = eg.gen(allArgs.get(i));
				emitStore(reg, offset, SP);
			}

			// Check for a null receiver
			// FIXME (from Michael) This copies the receiver's address in the
			// stack at SP, just as it's already done by the loop above. Can't
			// we remove this call to emitStore()?
			emitStore(reg, 0, SP); // check for null ptr
			call(CHECK_NULL, null);

			// Load the address of the method to call into "reg"
			// and call it indirectly.
			emitLoad(0, reg, reg);
			int mthdoffset = 4 + mthSymbol.vtableIndex * Config.SIZEOF_PTR;
			emitLoad(mthdoffset, reg, reg);

			call("*" + reg, reg);

			if (mthSymbol.returnType == main.voidType) {
				releaseRegister(reg);
				return null;
			}

			return reg;
		}

		@Override
		public String methodCall(MethodCall ast, Void dummy) {
			return methodCall(ast.sym, ast.allArguments());

		}

		// Emit vtable for arrays of this class:
		@Override
		public String classDecl(ClassDecl ast, Void arg) {
			// Emit each method:
			emitCommentSection("Class " + ast.name);
			return visitChildren(ast, arg);
		}

		@Override
		public String methodDecl(MethodDecl ast, Void arg) {
			emitMethodPrefix(ast);
			gen(ast.body());
			emitMethodSuffix(false);
			return null;
		}

		@Override
		public String ifElse(IfElse ast, Void arg) {
			String falseLbl = uniqueLabel();
			String doneLbl = uniqueLabel();
			genJumpIfFalse(ast.condition(), falseLbl);
			gen(ast.then());
			emit("jmp", doneLbl);
			emitLabel(falseLbl);
			gen(ast.otherwise());
			emitLabel(doneLbl);
			return null;

		}

		@Override
		public String whileLoop(WhileLoop ast, Void arg) {

			String nextLbl = uniqueLabel();
			String doneLbl = uniqueLabel();
			emitLabel(nextLbl);
			genJumpIfFalse(ast.condition(), doneLbl);
			gen(ast.body());
			emit("jmp", nextLbl);
			emitLabel(doneLbl);
			return null;

		}

		@Override
		public String assign(Assign ast, Void arg) {

			final String rhsReg = eg.gen(ast.right());
			class AssignVisitor extends ExprVisitor<Void, Void> {

				@Override
				public Void var(Var ast, Void arg) {
					emitStore(rhsReg, ast.sym.offset, BP);
					return null;
				}

				@Override
				public Void field(Field ast, Void arg) {
					String objReg = eg.gen(ast.arg());
					emitStore(objReg, 0, SP); // check for null ptr
					call(CHECK_NULL, null);
					emitStore(rhsReg, ast.sym.offset, objReg);
					releaseRegister(objReg);
					return null;
				}

				@Override
				public Void index(Index ast, Void arg) {
					String arrReg = eg.gen(ast.left());
					emitStore(arrReg, 0, SP);
					call(CHECK_NULL, null);
					String idxReg = eg.gen(ast.right());
					emitMove(rhsReg, a(arrReg, idxReg));
					releaseRegister(arrReg);
					releaseRegister(idxReg);
					return null;
				}

				@Override
				public Void methodCall(MethodCallExpr ast, Void arg) {
					emitMove(rhsReg, "%eax");
					return null;
				}

				@Override
				protected Void dfltExpr(Expr ast, Void arg) {
					throw new RuntimeException("Store to unexpected lvalue "
							+ ast);
				}

			}
			new AssignVisitor().visit(ast.left(), null);
			releaseRegister(rhsReg);
			return null;
		}

		@Override
		public String builtInWrite(BuiltInWrite ast, Void arg) {

			String reg = eg.gen(ast.arg());
			// Ensure that stack is aligned on 16 byte boundary at func
			// call:
			emitStore(reg, 4, SP);
			releaseRegister(reg);
			emitStore("$STR_D", 0, SP);
			call(PRINTF, null);
			return null;
		}

		@Override
		public String builtInWriteFloat(BuiltInWriteFloat ast, Void arg) {

			String reg = eg.gen(ast.arg());
			emitGprRegToFloatReg(FLOAT_REG_0, reg);
			emit("cvtps2pd", FLOAT_REG_0, FLOAT_REG_0);
			emitStoreDouble(FLOAT_REG_0, 4, SP);
			releaseRegister(reg);
			emitStore("$STR_F", 0, SP);
			call(PRINTF, null);
			return null;

		}

		@Override
		public String builtInWriteln(BuiltInWriteln ast, Void arg) {

			emitStore("$STR_NL", 0, SP);
			call(PRINTF, null);
			return null;

		}

		@Override
		public String returnStmt(ReturnStmt ast, Void arg) {
			if (ast.arg() != null) {
				String reg = eg.gen(ast.arg());
				emitMove(reg, "%eax");
				emitMethodSuffix(false);
			} else {
				emitMethodSuffix(true); // no return value -- return NULL as
										// a default (required for main())
			}
			return null;
		}

	}

	// -------------------------------------------------------
	// EMIT CODE

	private StringBuilder indent = new StringBuilder();

	/** Creates an constant operand relative to another operand. */
	protected String c(int i) {
		return "$" + i;
	}

	/** Creates an constant operand with the address of a label. */
	protected String c(String lbl) {
		return "$" + lbl;
	}

	/** Creates an operand relative to another operand. */
	protected String o(int offset, String reg) {
		return String.format("%d(%s)", offset, reg);
	}

	/** Creates an operand addressing an item in an array */
	protected String a(String arrReg, String idxReg) {
		final int offset = Config.SIZEOF_PTR; // one word in front for vptr
		final int mul = Config.SIZEOF_PTR; // assume all arrays of 4-byte elem
		return String.format("%d(%s,%s,%d)", offset, arrReg, idxReg, mul);
	}

	protected void emitIndent(String comment) {
		this.indent.append("  ");
		if (comment != null)
			emitComment(comment);
	}

	protected void emitCommentSection(String name) {
		int indentLen = this.indent.length();
		int breakLen = 68 - indentLen - name.length();
		StringBuffer sb = new StringBuffer();
		sb.append(Config.COMMENT_SEP).append(" ");
		for (int i = 0; i < indentLen; i++)
			sb.append("_");
		sb.append(name);
		for (int i = 0; i < breakLen; i++)
			sb.append("_");

		try {
			out.write(sb.toString());
			out.write("\n");
		} catch (IOException e) {
		}
	}

	protected void emitComment(String comment) {
		emit(Config.COMMENT_SEP + " " + comment);
	}

	protected void emitUndent() {
		this.indent.setLength(this.indent.length() - 2);
	}

	protected void emit(String op, String src, String dest) {
		emit(String.format("%s %s, %s", op, src, dest));
	}

	public void emit(String op, int src, String dest) {
		emit(op, c(src), dest);
	}

	protected void emit(String op, String dest) {
		emit(op + " " + dest);
	}

	protected void emit(String op, int dest) {
		emit(op, c(dest));
	}

	protected void emitMove(String src, String dest) {
		if (!src.equals(dest))
			emit("movl", src, dest);
	}

	protected void emitLoad(int srcOffset, String src, String dest) {
		emitMove(o(srcOffset, src), dest);
	}

	protected void emitStore(String src, int destOffset, String dest) {
		emitMove(src, o(destOffset, dest));
	}

	protected void emitMoveFloat(String src, String dest) {
		if (!src.equals(dest))
			emit("movss", src, dest);
	}

	protected void emitMoveDouble(String src, String dest) {
		if (!src.equals(dest))
			emit("movsd", src, dest);
	}

	protected void emitLoadFloat(int srcOffset, String src, String dest) {
		emitMoveFloat(o(srcOffset, src), dest);
	}

	protected void emitStoreFloat(String src, int destOffset, String dest) {
		emitMoveFloat(src, o(destOffset, dest));
	}

	private void emitGprRegToFloatReg(String dest, String src) {
		emitStore(src, tempOffset, BP);
		emitLoadFloat(tempOffset, BP, dest);
	}

	private void emitFloatRegToGprReg(String dest, String src) {
		emitStoreFloat(src, tempOffset, BP);
		emitLoad(tempOffset, BP, dest);
	}

	protected void emitStoreDouble(String src, int destOffset, String dest) {
		emitMoveDouble(src, o(destOffset, dest));
	}

	protected void emitConstantData(String data) {
		emit(String.format("%s %s", Config.DOT_INT, data));
	}

	private int counter = 0;

	protected String uniqueLabel() {
		String labelName = "label" + counter++;
		return labelName;
	}

	protected void emitLabel(String main) {
		try {
			out.write(main + ":" + "\n");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	protected void emit(String op) {
		try {
			out.write(indent.toString());
			out.write(op);
			out.write("\n");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	protected String mthdlbl(MethodSymbol msym) {
		return msym.owner.name + "_" + msym.name;
	}

	protected void emitMethodPrefix(MethodDecl ast) {
		// Emit the label for the method:
		emit(Config.TEXT_SECTION);
		emitCommentSection(String.format("Method %s.%s", ast.sym.owner.name,
				ast.name));
		emit(".globl " + mthdlbl(ast.sym));
		emitLabel(mthdlbl(ast.sym));

		// Compute the size and layout of the stack frame. Our
		// frame looks like (the numbers are relative to our ebp):
		//
		// (caller's locals)
		// arg N
		// ...
		// 12 arg 1
		// 8 arg 0 (this ptr)
		// 4 linkage ptr (return address)
		// 0 saved ebp
		// -4 locals
		// temporaries
		// (padding)
		// (callee's arguments)
		//
		// We allocate the entire stack frame all at once and
		// never adjust ESP during the course of a function call.
		// This is helpful for two reasons: (1) we could eliminate
		// EBP and gain another register (we don't because it
		// means that gdb works poorly) (2) we can easily guarantee
		// that the stack is 16-byte aligned at the point of all
		// function calls, which Max OS X requires.
		//
		// Since we cannot adjust the stack during the method,
		// however, we must find out in advance the number of
		// stack slots we will need. Stack slots fall into several
		// categories:
		// - "Linkage": overhead for function calls.
		// This includes the return address and saved ebp.
		// - locals: these store the value of user-declared local
		// variables.
		// - temporaries: these are stack slots used to store
		// values during expression evaluation when we run out
		// of registers, saving caller-saved registers, and
		// other miscellaneous purposes.
		// - padding: only there to ensure the stack size is a multiple
		// of 16.
		// - arguments: values we will pass to functions being
		// invoked.
		//
		// We calculate all address relative to the base pointer
		// except for arguments, which we address relative to the
		// stack pointer. If we wanted to use EBP as a general
		// purpose register, we would switch to calculating all
		// addresses relative to ESP.

		// Assign parameter offsets:
		// As shown above, these start from 12, since the
		// first parameter (offset 8) is the this ptr.
		int paramOffset = Config.SIZEOF_PTR * 3;
		for (VariableSymbol param : ast.sym.parameters) {
			param.offset = paramOffset;
			paramOffset += Config.SIZEOF_PTR;
		}

		// First few slots are reserved for caller save regs:
		int localSlot = callerSave.length * SIZEOF_REG;

		// Assign local variable offsets:
		emitComment(String.format("%-10s   Offset", "Variable"));
		for (VariableSymbol lcl : ast.sym.locals.values()) {
			lcl.offset = -localSlot;
			localSlot += Config.SIZEOF_PTR;
			emitComment(String.format("%-10s   %d", lcl, lcl.offset));
		}

		// Record maximum local variable slot as the location for the
		// next temporary, and figure out how many temporaries we will
		// need. We always guarantee at least enough temps for
		// caller save registers to be preserved at any time.
		tempOffset = -localSlot;
		int maxRegs = rnv.calc(ast);
		int temps = max(0, maxRegs - registers.size()) + callerSave.length
				+ calleeSave.length;
		localSlot += temps * Config.SIZEOF_PTR;

		emitComment(String.format("maxRegs=%d callerSave.length=%d temps=%d",
				maxRegs, callerSave.length, temps));

		// Find out how many parameters we will have:
		int numParams = new ArgsNeededVisitor().visit(ast, null);

		// Round up stack size to make it a multiple of 16.
		// The actual amount passed to the enter instruction is 8
		// less, however, because it wants to know the amount
		// in addition to the linkage ptr and saved ebp.
		int implicit = Config.SIZEOF_PTR * 2;
		int stackSize = (implicit + localSlot + numParams + 15) & 0xFFFFFFF0;
		stackSize -= implicit;

		emitComment(String.format(
				"implicit=%d localSlot=%d numParams=%d sum=%d", implicit,
				localSlot, numParams, implicit + localSlot + numParams));

		emit(String.format("enter $%d, $0", stackSize));

		storeCallSaveRegs();

	}

	protected void emitMethodSuffix(boolean returnNull) {
		if (returnNull)
			emit("movl", "$0", "%eax");
		restoreCalleeSaveRegs();
		emit("leave");
		emit("ret");
	}

}
