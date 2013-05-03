package cd;

import java.io.File;
import java.util.List;

import cd.ir.ast.ClassDecl;
import cd.ir.symbols.ClassSymbol;
import cd.semantic.TypeSymbolTable;

/**
 * The data that comprises a single run of the compiler on a file.
 * 
 */
public class CompilationContext {
	private final File sourceFile;
	private final File assemblyFile;
	private final File binaryFile;
	// Set to non-null to write dump of control flow graph
	private final File cfgDumpBase;
	
	private final CompilerOptions options;

	/** List of all type symbols, used by code generator. */
	private TypeSymbolTable typeSymbols;
	
	/** Symbol for the Main type */
	private ClassSymbol mainType;
	
	private List<ClassDecl> astRoots;


	/**
	 * Constructor to be used for testing purposes
	 */
	public CompilationContext() {
		this.sourceFile = null;
		this.assemblyFile = null;
		this.binaryFile = null;
		this.cfgDumpBase = null;
		this.options = new CompilerOptions();
	}
	
	public CompilationContext(File file) {
		this(file, new CompilerOptions());
	}

	public CompilationContext(File file, CompilerOptions options) {
		this.sourceFile = file;
		this.assemblyFile = new File(file.getPath() + Config.ASMEXT);
		this.binaryFile = new File(file.getPath() + Config.BINARYEXT);
		this.cfgDumpBase = file;
		this.options = options;
	}

	public TypeSymbolTable getTypeSymbols() {
		return typeSymbols;
	}

	public File getCfgDumpBase() {
		return cfgDumpBase;
	}

	public File getSourceFile() {
		return sourceFile;
	}

	public void deleteIntermediateFiles() {
		// Delete intermediate files from previous runs:
		if (assemblyFile.exists())
			assemblyFile.delete();
		if (binaryFile.exists())
			binaryFile.delete();		
	}

	public File getAssemblyFile() {
		return assemblyFile;
	}

	public File getBinaryFile() {
		return binaryFile;
	}

	public void setTypeSymbols(TypeSymbolTable typeSymbols) {
		this.typeSymbols = typeSymbols;
	}

	public ClassSymbol getMainType() {
		return mainType;
	}

	public void setMainType(ClassSymbol mainType) {
		this.mainType = mainType;
	}

	public List<ClassDecl> getAstRoots() {
		return astRoots;
	}

	public void setAstRoots(List<ClassDecl> astRoots) {
		this.astRoots = astRoots;
	}

	public CompilerOptions getOptions() {
		return options;
	}
}