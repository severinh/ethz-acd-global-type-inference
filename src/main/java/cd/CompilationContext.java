package cd;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import cd.ir.ast.ClassDecl;
import cd.ir.symbols.ClassSymbol;
import cd.semantic.TypeSymbolTable;

/**
 * The data that comprises a single run of the compiler on a file.
 */
public class CompilationContext {

	private final File sourceFile;
	private final File assemblyFile;
	private final File binaryFile;

	@Nullable
	private final File cfgDumpBase;

	private final CompilerOptions options;

	/** List of all type symbols, used by code generator. */
	private TypeSymbolTable typeSymbols;

	/** Symbol for the Main type */
	@Nullable
	private ClassSymbol mainType;

	private List<ClassDecl> classDecls;

	public CompilationContext(File file) {
		this(file, new CompilerOptions());
	}

	public CompilationContext(File file, CompilerOptions options) {
		this.sourceFile = file;
		this.assemblyFile = new File(file.getPath() + Config.ASMEXT);
		this.binaryFile = new File(file.getPath() + Config.BINARYEXT);
		this.cfgDumpBase = (options.isDebugging()) ? file : null;
		this.options = options;
		this.typeSymbols = new TypeSymbolTable();
		this.classDecls = new ArrayList<>();

	}

	public TypeSymbolTable getTypeSymbols() {
		return typeSymbols;
	}

	@Nullable
	public File getCfgDumpBase() {
		return cfgDumpBase;
	}

	public File getSourceFile() {
		return sourceFile;
	}

	/**
	 * Delete intermediate files from previous runs.
	 */
	public void deleteIntermediateFiles() {
		if (assemblyFile.exists()) {
			assemblyFile.delete();
		}

		if (binaryFile.exists()) {
			binaryFile.delete();
		}
	}

	public File getAssemblyFile() {
		return assemblyFile;
	}

	public File getBinaryFile() {
		return binaryFile;
	}

	@Nullable
	public ClassSymbol getMainType() {
		return mainType;
	}

	public void setMainType(ClassSymbol mainType) {
		this.mainType = mainType;
	}

	public List<ClassDecl> getAstRoots() {
		return classDecls;
	}

	/**
	 * Add a class declaration to the context.
	 * 
	 * The method is package-private because it is meant to be called by the
	 * {@link CompilerToolchain}.
	 */
	void addClassDecl(ClassDecl classDecl) {
		this.classDecls.add(classDecl);
	}

	/**
	 * Add a list of class declarations to the context.
	 * 
	 * The method is package-private because it is meant to be called by the
	 * {@link CompilerToolchain}.
	 */
	void addClassDecls(List<ClassDecl> classDecls) {
		this.classDecls.addAll(classDecls);
	}

	public CompilerOptions getOptions() {
		return options;
	}

}