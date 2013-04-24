package cd.semantic;

import cd.Main;
import cd.debug.AstOneLine;
import cd.exceptions.SemanticFailure;
import cd.exceptions.SemanticFailure.Cause;
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
import cd.ir.Symbol.PrimitiveTypeSymbol;
import cd.ir.Symbol.TypeSymbol;
import cd.ir.Symbol.VariableSymbol;

public class TypeChecker {

	final Main main;
	final SymTable<TypeSymbol> typeSymbols;
	final TypingVisitor visitor = new TypingVisitor();
	
	private MethodDecl thisMethod;
	
	public TypeChecker(Main main, SymTable<TypeSymbol> typeSymbols) {
		this.main = main;
		this.typeSymbols = typeSymbols;
	}

	public TypeSymbol type(Expr expr, SymTable<VariableSymbol> locals) {
		return visitor.visit(expr, locals);
	}
	
	/**
	 * Checks whether two expressions have the same type
	 * and throws an exception otherwise.
	 * @return The common type of the two expression.
	 */
	public TypeSymbol typeEquality(Expr leftExpr, Expr rightExpr, SymTable<VariableSymbol> locals) {
		
		final TypeSymbol leftType = type(leftExpr, locals);
		final TypeSymbol rightType = type(rightExpr, locals);

		if (leftType != rightType) {
							
			throw new SemanticFailure(
						Cause.TYPE_ERROR, 
						"Expected operand types to be equal but found %s, %s",
						leftType, 
						rightType);
		}
	
		return leftType;
		
	}
	
	public void typeIsPrimitive(TypeSymbol type) {
		
		if (type != main.intType && type != main.floatType) {
			
			throw new SemanticFailure(
					Cause.TYPE_ERROR, 
					"Expected %s or %s for operands but found type %s", 
					main.intType, 
					main.floatType, 
					type);	
		}
		
	}
	
	public void typeIsValidForOperator(TypeSymbol type, BOp operator) {
		
		if (operator == BOp.B_MOD && type == main.floatType) {
			
			throw new SemanticFailure(
					Cause.TYPE_ERROR, 
					"Operation %s not valid for type %s", 
					operator, 
					type);	
			
		}
		
	}
	
	public ClassSymbol asClass(TypeSymbol type) {
		if (type instanceof ClassSymbol)
			return (ClassSymbol) type;
		throw new SemanticFailure(
				Cause.TYPE_ERROR, 
				"A class type was required, but %s was found", type);
	}

	public ArrayTypeSymbol asArray(TypeSymbol type) {
		if (type instanceof ArrayTypeSymbol)
			return (ArrayTypeSymbol) type;
		throw new SemanticFailure(
				Cause.TYPE_ERROR, 
				"An array type was required, but %s was found", type);
	}
	
	public TypeSymbol superType(TypeSymbol sym) {
		if (sym instanceof PrimitiveTypeSymbol)
			return null;
		
		if (sym instanceof ArrayTypeSymbol)
			return main.objectType;
		
		return ((ClassSymbol)sym).superClass;
	}
	
	public boolean subtype(TypeSymbol sup, TypeSymbol sub) {
		if (sub == main.nullType && sup.isReferenceType())
			return true;
		while (sub != null) {
			if (sub == sup) return true;
			sub = superType(sub);
		}
		return false;
	}
	
	public void checkType(Expr ast, TypeSymbol expected, SymTable<VariableSymbol> locals) {
		TypeSymbol actual = type(ast, locals);
		if (!subtype(expected, actual))
			throw new SemanticFailure(
					Cause.TYPE_ERROR,
					"Expected %s but type was %s",
					expected,
					actual);
	}
		
	public void checkStmt(MethodDecl method, SymTable<VariableSymbol> locals) {
		this.thisMethod = method;		
		new StmtVisitor().visit(method.body(), locals);
	}
	
	public class StmtVisitor extends AstVisitor<Void, SymTable<VariableSymbol>> {
		
		@Override
		protected Void dfltExpr(Expr ast, SymTable<VariableSymbol> locals) {
			throw new RuntimeException("Should not get here");
		}
		
		@Override
		public Void assign(Assign ast, SymTable<VariableSymbol> locals) {
			TypeSymbol lhs = typeLhs(ast.left(), locals);
			TypeSymbol rhs = type(ast.right(), locals);
			if (!subtype(lhs, rhs))
				throw new SemanticFailure(
						Cause.TYPE_ERROR,
						"Type %s cannot be assigned to %s",
						rhs, lhs);
			return null;
		}

		@Override
		public Void builtInWrite(BuiltInWrite ast, SymTable<VariableSymbol> locals) {
			checkType(ast.arg(), main.intType, locals);
			return null;
		}
		
		@Override
		public Void builtInWriteFloat(BuiltInWriteFloat ast, SymTable<VariableSymbol> locals) {
			checkType(ast.arg(), main.floatType, locals);
			return null;
		}

		@Override
		public Void builtInWriteln(BuiltInWriteln ast, SymTable<VariableSymbol> locals) {
			return null;
		}
		
		@Override
		public Void ifElse(IfElse ast, SymTable<VariableSymbol> locals) {
			checkType(ast.condition(), main.booleanType, locals);
			visit(ast.then(), locals);
			if (ast.otherwise() != null)
				visit(ast.otherwise(), locals);
			return null;
		}

		@Override
		public Void methodCall(MethodCall ast, SymTable<VariableSymbol> locals) {
			
			ClassSymbol rcvrType = asClass(type(ast.receiver(), locals));			
			MethodSymbol mthd = rcvrType.getMethod(ast.methodName);
			if (mthd == null)
				throw new SemanticFailure(
						Cause.NO_SUCH_METHOD,
						"Class %s has no method %s()",
						rcvrType.name, ast.methodName);		
			
			ast.sym = mthd;
			
			// Check that the number of arguments is correct.
			if (ast.argumentsWithoutReceiver().size() != mthd.parameters.size())
				throw new SemanticFailure(
						Cause.WRONG_NUMBER_OF_ARGUMENTS,
						"Method %s() takes %d arguments, but was invoked with %d",
						ast.methodName,
						mthd.parameters.size(),
						ast.argumentsWithoutReceiver().size());
									
			// Check that the arguments are of correct type.
			int i = 0;
			for (Ast argAst : ast.argumentsWithoutReceiver())
				checkType((Expr)argAst, mthd.parameters.get(i++).type, locals);
			
			return null;
			
		}

		@Override
		public Void whileLoop(WhileLoop ast, SymTable<VariableSymbol> locals) {
			checkType(ast.condition(), main.booleanType, locals);
			return visit(ast.body(), locals);
		}
		
		@Override
		public Void returnStmt(ReturnStmt ast, SymTable<VariableSymbol> locals) {
			
			if (ast.arg() == null) {

				if (thisMethod.sym.returnType != main.voidType) {
					
					throw new SemanticFailure(
							Cause.TYPE_ERROR,
							"Return statement has no arguments. Expected %s but type was %s",
							thisMethod.sym.returnType,
							main.voidType);
				}
				
			} else {
				checkType(ast.arg(), thisMethod.sym.returnType, locals);
			}
			
			return null;
			
		}
		
	}

	public class TypingVisitor extends ExprVisitor<TypeSymbol, SymTable<VariableSymbol>> {
		
		@Override
		public TypeSymbol visit(Expr ast, SymTable<VariableSymbol> arg) {
			ast.type = super.visit(ast, arg);
			return ast.type;
		}
 
		@Override
		public TypeSymbol binaryOp(BinaryOp ast, SymTable<VariableSymbol> locals) {
			
			switch (ast.operator) {
						
			case B_TIMES:
			case B_DIV:
			case B_MOD:
			case B_PLUS:
			case B_MINUS:
			{	
				TypeSymbol type = typeEquality(ast.left(), ast.right(), locals);
				typeIsPrimitive(type);
				typeIsValidForOperator(type, ast.operator);				
				return type;
			}
			
			case B_AND:
			case B_OR:
	        	checkType(ast.left(), main.booleanType, locals);
    			checkType(ast.right(), main.booleanType, locals);
    			return main.booleanType;
	        	
			case B_EQUAL:
			case B_NOT_EQUAL:
	        	TypeSymbol left = type(ast.left(), locals);
	        	TypeSymbol right = type(ast.right(), locals);
	        	if (subtype(left, right) || subtype(right,left))
	        		return main.booleanType;
	        	throw new SemanticFailure(
	        			Cause.TYPE_ERROR,
	        			"Types %s and %s could never be equal",
	        			left, right);
	        	
			case B_LESS_THAN:
			case B_LESS_OR_EQUAL:
			case B_GREATER_THAN:
			case B_GREATER_OR_EQUAL:
			{	
				TypeSymbol type = typeEquality(ast.left(), ast.right(), locals);
				typeIsPrimitive(type);
    			return main.booleanType;
			}
			
			}
			throw new RuntimeException("Unhandled operator "+ast.operator);
		}

		@Override
		public TypeSymbol booleanConst(BooleanConst ast, SymTable<VariableSymbol> arg) {
			return main.booleanType;
		}

		@Override
		public TypeSymbol builtInRead(BuiltInRead ast, SymTable<VariableSymbol> arg) {
			return main.intType;
		}
		
		@Override
		public TypeSymbol builtInReadFloat(BuiltInReadFloat ast, SymTable<VariableSymbol> arg) {
			return main.floatType;
		}

		@Override
		public TypeSymbol cast(Cast ast, SymTable<VariableSymbol> locals) {
			TypeSymbol argType = type(ast.arg(), locals);
			ast.typeSym = typeSymbols.getType(ast.typeName);
			
			if (subtype(argType, ast.typeSym) ||
					subtype(ast.typeSym, argType))
				return ast.typeSym;
			
			throw new SemanticFailure(
					Cause.TYPE_ERROR,
					"Types %s and %s in cast are completely unrelated.",
					argType, ast.typeSym);
		}

		@Override
		protected TypeSymbol dfltExpr(Expr ast, SymTable<VariableSymbol> arg) {
			throw new RuntimeException("Unhandled type");
		}

		@Override
		public TypeSymbol field(Field ast, SymTable<VariableSymbol> locals) {
			ClassSymbol argType = asClass(type(ast.arg(), locals));  // class of the receiver of the field access
			ast.sym = argType.getField(ast.fieldName);
			if (ast.sym == null)
				throw new SemanticFailure(
						Cause.NO_SUCH_FIELD,
						"Type %s has no field %s",
						argType, ast.fieldName);
			return ast.sym.type;
		}

		@Override
		public TypeSymbol index(Index ast, SymTable<VariableSymbol> locals) {
			ArrayTypeSymbol argType = asArray(type(ast.left(), locals));
			checkType(ast.right(), main.intType, locals);
			return argType.elementType;
		}

		@Override
		public TypeSymbol intConst(IntConst ast, SymTable<VariableSymbol> arg) {
			return main.intType;
		}
		
		@Override
		public TypeSymbol floatConst(FloatConst ast, SymTable<VariableSymbol> arg) {
			return main.floatType;
		}

		@Override
		public TypeSymbol newArray(NewArray ast, SymTable<VariableSymbol> locals) {
			checkType(ast.arg(), main.intType, locals);
			return typeSymbols.getType(ast.typeName);
		}

		@Override
		public TypeSymbol newObject(NewObject ast, SymTable<VariableSymbol> arg) {
			return typeSymbols.getType(ast.typeName);
		}

		@Override
		public TypeSymbol nullConst(NullConst ast, SymTable<VariableSymbol> arg) {
			return main.nullType;
		}

		@Override
		public TypeSymbol thisRef(ThisRef ast, SymTable<VariableSymbol> locals) {
			VariableSymbol vsym = locals.get("this");
			return vsym.type;
		}

		@Override
		public TypeSymbol unaryOp(UnaryOp ast, SymTable<VariableSymbol> locals) {
			
			switch (ast.operator) {
			case U_PLUS:
			case U_MINUS:
			{
				TypeSymbol type = type(ast.arg(), locals);
				typeIsPrimitive(type);			
				return type;
			}
			
			case U_BOOL_NOT:
				checkType(ast.arg(), main.booleanType, locals);
				return main.booleanType;			
			}
			throw new RuntimeException("Unknown unary op "+ast.operator);
		}

		@Override
		public TypeSymbol var(Var ast, SymTable<VariableSymbol> locals) {
			if (!locals.contains(ast.name))
				throw new SemanticFailure(
						Cause.NO_SUCH_VARIABLE,
						"No variable %s was found",
						ast.name);
			ast.setSymbol(locals.get(ast.name));
			return ast.sym.type;
		}
		
		@Override
		public TypeSymbol methodCall(MethodCallExpr ast, SymTable<VariableSymbol> locals) {

			ClassSymbol rcvrType = asClass(type(ast.receiver(), locals));			
			MethodSymbol mthd = rcvrType.getMethod(ast.methodName);
			if (mthd == null)
				throw new SemanticFailure(
						Cause.NO_SUCH_METHOD,
						"Class %s has no method %s()",
						rcvrType.name, ast.methodName);		
			
			ast.sym = mthd;
			
			// Check that the number of arguments is correct.
			if (ast.argumentsWithoutReceiver().size() != mthd.parameters.size())
				throw new SemanticFailure(
						Cause.WRONG_NUMBER_OF_ARGUMENTS,
						"Method %s() takes %d arguments, but was invoked with %d",
						ast.methodName,
						mthd.parameters.size(),
						ast.argumentsWithoutReceiver().size());
									
			// Check that the arguments are of correct type.
			int i = 0;
			for (Ast argAst : ast.argumentsWithoutReceiver())
				checkType((Expr)argAst, mthd.parameters.get(i++).type, locals);
			
			return ast.sym.returnType;
			
		}

	}
	
	/**
	 * Evaluates an expr as the left-hand-side of an assignment,
	 * returning the type of value that may be assigned there.
	 * May fail if the expression is not a valid LHS (for example,
	 * a "final" field). */
	public TypeSymbol typeLhs(Expr expr, SymTable<VariableSymbol> locals) {
		return new LValueVisitor().visit(expr, locals);
	}
	
	/**
	 * @see TypeChecker#typeLhs(Expr, SymTable)
	 */
	class LValueVisitor extends ExprVisitor<TypeSymbol, SymTable<VariableSymbol>> {

		/** Any other kind of expression is not a value lvalue */
		@Override
		protected TypeSymbol dfltExpr(Expr ast, SymTable<VariableSymbol> locals) {
			throw new SemanticFailure(
					Cause.NOT_ASSIGNABLE,
					"'%s' is not a valid lvalue", 
					AstOneLine.toString(ast));
		}

		/** A field can always be assigned to. */
		@Override
		public TypeSymbol field(Field ast, SymTable<VariableSymbol> locals) {
			TypeSymbol ts = type(ast, locals);
			return ts;
		}

		/** 
		 * An array dereference can always be assigned to
		 */
		@Override
		public TypeSymbol index(Index ast, SymTable<VariableSymbol> locals) {
			return type(ast, locals);
		}

		/** A variable can always be assigned to. */
		@Override
		public TypeSymbol var(Var ast, SymTable<VariableSymbol> locals) {
			TypeSymbol ts = type(ast, locals);
			return ts;
		}
		
	}

}

