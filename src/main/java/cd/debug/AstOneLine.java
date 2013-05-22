package cd.debug;

import cd.ir.AstVisitor;
import cd.ir.ast.Assign;
import cd.ir.ast.Ast;
import cd.ir.ast.BinaryOp;
import cd.ir.ast.BooleanConst;
import cd.ir.ast.BuiltInRead;
import cd.ir.ast.BuiltInReadFloat;
import cd.ir.ast.BuiltInTick;
import cd.ir.ast.BuiltInTock;
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
import cd.ir.ast.Nop;
import cd.ir.ast.NullConst;
import cd.ir.ast.ReturnStmt;
import cd.ir.ast.Seq;
import cd.ir.ast.ThisRef;
import cd.ir.ast.UnaryOp;
import cd.ir.ast.Var;
import cd.ir.ast.VarDecl;
import cd.ir.ast.WhileLoop;

public class AstOneLine {

	public static String toString(Ast ast) {
		return new Visitor().visit(ast, null);
	}

	protected static class Visitor extends AstVisitor<String, Void> {

		public String str(Ast ast) {
			return ast.accept(this, null);
		}

		@Override
		public String assign(Assign ast, Void arg) {
			return String.format("%s = %s", str(ast.left()), str(ast.right()));
		}

		@Override
		public String binaryOp(BinaryOp ast, Void arg) {
			return String.format("(%s %s %s)", str(ast.left()),
					ast.operator.repr, str(ast.right()));
		}

		@Override
		public String booleanConst(BooleanConst ast, Void arg) {
			return Boolean.toString(ast.value);
		}

		@Override
		public String builtInRead(BuiltInRead ast, Void arg) {
			return String.format("read()");
		}

		@Override
		public String builtInReadFloat(BuiltInReadFloat ast, Void arg) {
			return String.format("readf()");
		}

		@Override
		public String builtInWrite(BuiltInWrite ast, Void arg) {
			return String.format("write(%s)", str(ast.arg()));
		}

		@Override
		public String builtInWriteFloat(BuiltInWriteFloat ast, Void arg) {
			return String.format("writef(%s)", str(ast.arg()));
		}

		@Override
		public String builtInWriteln(BuiltInWriteln ast, Void arg) {
			return String.format("writeln()");
		}
		
		@Override
		public String builtInTick(BuiltInTick ast, Void arg) {
			return String.format("tick()");
		}
		
		@Override
		public String builtInTock(BuiltInTock ast, Void arg) {
			return String.format("tock()");
		}

		@Override
		public String cast(Cast ast, Void arg) {
			return String.format("(%s)(%s)", ast.typeName, str(ast.arg()));
		}

		@Override
		public String classDecl(ClassDecl ast, Void arg) {
			return String.format("class %s {...}", ast.name);
		}

		@Override
		public String field(Field ast, Void arg) {
			return String.format("%s.%s", str(ast.arg()), ast.fieldName);
		}

		@Override
		public String ifElse(IfElse ast, Void arg) {
			return String.format("if (%s) {...} else {...}",
					str(ast.condition()));
		}

		@Override
		public String index(Index ast, Void arg) {
			return String.format("%s[%s]", str(ast.left()), str(ast.right()));
		}

		@Override
		public String intConst(IntConst ast, Void arg) {
			return Integer.toString(ast.value);
		}

		@Override
		public String floatConst(FloatConst ast, Void arg) {
			return String.format("%.10f", ast.value);
		}

		@Override
		public String methodCall(MethodCall ast, Void arg) {
			return String.format("%s.%s(...)", str(ast.receiver()),
					ast.methodName);
		}

		@Override
		public String methodCall(MethodCallExpr ast, Void arg) {
			return String.format("%s.%s(...)", str(ast.receiver()),
					ast.methodName);
		}

		@Override
		public String methodDecl(MethodDecl ast, Void arg) {
			return String.format("%s %s(...) {...}", ast.returnType, ast.name);
		}

		@Override
		public String newArray(NewArray ast, Void arg) {
			return String.format("new %s[%s]", ast.typeName, str(ast.arg()));
		}

		@Override
		public String newObject(NewObject ast, Void arg) {
			return String.format("new %s()", ast.typeName);
		}

		@Override
		public String nop(Nop ast, Void arg) {
			return "nop";
		}

		@Override
		public String nullConst(NullConst ast, Void arg) {
			return "null";
		}

		@Override
		public String seq(Seq ast, Void arg) {
			return "(...)";
		}

		@Override
		public String thisRef(ThisRef ast, Void arg) {
			return "this";
		}

		@Override
		public String returnStmt(ReturnStmt ast, Void arg) {
			return ast.arg() != null ? String.format("return %s",
					str(ast.arg())) : "return";
		}

		@Override
		public String unaryOp(UnaryOp ast, Void arg) {
			return String.format("%s(%s)", ast.operator.repr, str(ast.arg()));
		}

		@Override
		public String var(Var ast, Void arg) {
			if (ast.getSymbol() != null) {
				String symName = ast.getSymbol().toString();
				if (ast.getName() == null || ast.getName().equals(symName))
					return symName;

				// Return something strange to warn about the mismatch here:
				return String.format("(%s!=%s)", symName, ast.getName());
			} else
				return ast.getName();
		}

		@Override
		public String varDecl(VarDecl ast, Void arg) {
			return String.format("%s %s", ast.type, ast.name);
		}

		@Override
		public String whileLoop(WhileLoop ast, Void arg) {
			return String.format("while (%s) {...}", str(ast.condition()));
		}

	}
}
