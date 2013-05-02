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
	public File sourceFile;
	public File assemblyFile;
	public File binaryFile;
	// Set to non-null to write dump of control flow graph
	public File cfgDumpBase;

	/** List of all type symbols, used by code generator. */
	public TypeSymbolTable typeSymbols;
	
	/** Symbol for the Main type */
	public ClassSymbol mainType;
	
	public List<ClassDecl> astRoots;


	public TypeSymbolTable getTypeSymbols() {
		return typeSymbols;
	}

	public File getCfgDumpBase() {
		return cfgDumpBase;
	}

	public CompilationContext() {
	}

	public CompilationContext(File file) {
		this.sourceFile = file;
		this.assemblyFile = new File(file.getPath() + Config.ASMEXT);
		this.binaryFile = new File(file.getPath() + Config.BINARYEXT);
		this.cfgDumpBase = file;	
	}

	public void deleteIntermediateFiles() {
		// Delete intermediate files from previous runs:
		if (assemblyFile.exists())
			assemblyFile.delete();
		if (binaryFile.exists())
			binaryFile.delete();		
	}
}