package cd.codegen;

import cd.Config;
import cd.ir.Ast;
import cd.ir.Ast.BuiltInRead;
import cd.ir.Ast.BuiltInReadFloat;
import cd.ir.Ast.BuiltInWrite;
import cd.ir.Ast.BuiltInWriteFloat;
import cd.ir.Ast.BuiltInWriteln;
import cd.ir.Ast.Field;
import cd.ir.Ast.Index;
import cd.ir.Ast.MethodCall;
import cd.ir.Ast.MethodCallExpr;
import cd.ir.Ast.NewArray;
import cd.ir.Ast.NewObject;
import cd.ir.AstVisitor;

/**
 * This visitor computes, for a given method body, the maximum number of bytes
 * of arguments pushed by any function call within the method. This is used when
 * constructing the stack frame to find out how many bytes to reserve for
 * functions that are invoked.
 */
public class ArgsNeededVisitor extends AstVisitor<Integer, Void> {

	@Override
	public Integer methodCall(MethodCall ast, Void arg) {
		return ast.allArguments().size() * Config.SIZEOF_PTR;
	}

	@Override
	public Integer methodCall(MethodCallExpr ast, Void arg) {
		return ast.allArguments().size() * Config.SIZEOF_PTR;
	}

	@Override
	public Integer field(Field ast, Void arg) {
		// have to check for null ptrs
		return Config.SIZEOF_PTR;
	}

	@Override
	public Integer index(Index ast, Void arg) {
		// have to check for index boundaries
		return Config.SIZEOF_PTR * 2;
	}

	@Override
	public Integer builtInWrite(BuiltInWrite ast, Void arg) {
		return Config.SIZEOF_PTR * 2; // calls printf("%d\n", x)
	}

	@Override
	public Integer builtInWriteFloat(BuiltInWriteFloat ast, Void arg) {
		return Config.SIZEOF_PTR * 2; // calls printf("%.5f\n", x)
	}

	@Override
	public Integer builtInWriteln(BuiltInWriteln ast, Void arg) {
		return Config.SIZEOF_PTR; // calls printf("\n")
	}

	@Override
	public Integer builtInRead(BuiltInRead ast, Void arg) {
		return Config.SIZEOF_PTR * 3; // calls sscanf("%d", &x), incl. slot for
										// &x
	}

	@Override
	public Integer builtInReadFloat(BuiltInReadFloat ast, Void arg) {
		return Config.SIZEOF_PTR * 4; // calls sscanf("%f", &x), incl. slot for
										// &x (it's a double)
	}

	@Override
	public Integer newArray(NewArray ast, Void arg) {
		return Config.SIZEOF_PTR; // calls malloc()
	}

	@Override
	public Integer newObject(NewObject ast, Void arg) {
		return Config.SIZEOF_PTR; // calls malloc()
	}

	@Override
	public Integer visitChildren(Ast ast, Void arg) {
		// Find the max of any children:
		int max = 0;
		for (Ast chi : ast.children())
			max = Math.max(max, visit(chi, arg));
		return max;
	}

}
