package cd.codegen;

import cd.Config;
import cd.ir.ast.Ast;
import cd.ir.ast.BuiltInRead;
import cd.ir.ast.BuiltInReadFloat;
import cd.ir.ast.BuiltInWrite;
import cd.ir.ast.BuiltInWriteFloat;
import cd.ir.ast.BuiltInWriteln;
import cd.ir.ast.Field;
import cd.ir.ast.Index;
import cd.ir.ast.MethodCall;
import cd.ir.ast.MethodCallExpr;
import cd.ir.ast.NewArray;
import cd.ir.ast.NewObject;
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
