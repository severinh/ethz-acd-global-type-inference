package cd.semantic;

import java.util.HashSet;
import java.util.Set;

import cd.exceptions.SemanticFailure;
import cd.exceptions.SemanticFailure.Cause;
import cd.ir.ast.ClassDecl;
import cd.ir.ast.MethodDecl;
import cd.ir.ast.VarDecl;
import cd.ir.symbols.ClassSymbol;
import cd.ir.symbols.MethodSymbol;
import cd.ir.symbols.VariableSymbol;
import cd.ir.symbols.VariableSymbol.Kind;
import cd.ir.AstVisitor;

/**
 * A helper class for {@link SemanticAnalyzer} which iterates through a class
 * definition and creates symbols for all of its fields and methods. For each
 * method, we also create symbols for parameters and local variables.
 */
public class SymbolCreator extends Object {

	private final TypeSymbolTable typeSymbols;
	private final ClassSymbolCreator classSymbolCreator;
	private final MethodSymbolCreator methodSymbolCreator;

	public SymbolCreator(TypeSymbolTable typesTable) {
		this.typeSymbols = typesTable;
		this.classSymbolCreator = new ClassSymbolCreator();
		this.methodSymbolCreator = new MethodSymbolCreator();
	}

	public void createSymbols(ClassDecl cd) {
		classSymbolCreator.visitChildren(cd, cd.sym);
	}

	/**
	 * Creates symbols for all fields/constants/methods in a class. Uses
	 * {@link MethodSymbolCreator} to create symbols for all parameters and
	 * local variables to each method as well. Checks for duplicate members.
	 */
	private class ClassSymbolCreator extends AstVisitor<Void, ClassSymbol> {

		@Override
		public Void varDecl(VarDecl varDecl, ClassSymbol classSymbol) {
			VariableSymbol variableSymbol = new VariableSymbol(varDecl.name,
					typeSymbols.getType(varDecl.type), Kind.FIELD);
			varDecl.sym = variableSymbol;
			classSymbol.addField(variableSymbol);
			return null;
		}

		@Override
		public Void methodDecl(MethodDecl methodDecl, ClassSymbol classSymbol) {
			MethodSymbol methodSymbol = new MethodSymbol(methodDecl.name,
					classSymbol);
			methodDecl.sym = methodSymbol;
			classSymbol.addMethod(methodSymbol);

			// create return type symbol
			if (methodDecl.returnType.equals("void")) {
				methodDecl.sym.returnType = typeSymbols.getVoidType();
			} else {
				methodDecl.sym.returnType = typeSymbols
						.getType(methodDecl.returnType);
			}

			// create symbols for each parameter
			Set<String> pnames = new HashSet<>();
			for (int i = 0; i < methodDecl.argumentNames.size(); i++) {
				String argumentName = methodDecl.argumentNames.get(i);
				String argumentType = methodDecl.argumentTypes.get(i);
				if (pnames.contains(argumentName))
					throw new SemanticFailure(Cause.DOUBLE_DECLARATION,
							"Method '%s' has two parameters named '%s'",
							methodDecl.sym, argumentName);
				pnames.add(argumentName);
				VariableSymbol vs = new VariableSymbol(argumentName,
						typeSymbols.getType(argumentType));
				methodDecl.sym.addParameter(vs);
			}

			// create symbols for the local variables
			methodSymbolCreator.visitChildren(methodDecl.decls(),
					methodDecl.sym);

			return null;
		}

	}

	private class MethodSymbolCreator extends AstVisitor<Void, MethodSymbol> {

		@Override
		public Void methodDecl(MethodDecl methodDecl, MethodSymbol methodSymbol) {
			throw new SemanticFailure(Cause.NESTED_METHODS_UNSUPPORTED,
					"Nested methods are not supported.");
		}

		@Override
		public Void varDecl(VarDecl varDecl, MethodSymbol methodSymbol) {
			varDecl.sym = new VariableSymbol(varDecl.name,
					typeSymbols.getType(varDecl.type), Kind.LOCAL);
			methodSymbol.addLocal(varDecl.sym);
			return null;
		}

	}
}
