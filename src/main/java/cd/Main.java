package cd;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.antlr.runtime.ANTLRReaderStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTreeNodeStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cd.cfg.CFGBuilder;
import cd.cfg.DeSSA;
import cd.cfg.Dominator;
import cd.cfg.Optimizer;
import cd.cfg.SSA;
import cd.codegen.CfgCodeGenerator;
import cd.debug.AstDump;
import cd.debug.CfgDump;
import cd.exceptions.ParseFailure;
import cd.ir.ast.ClassDecl;
import cd.ir.ast.MethodDecl;
import cd.ir.symbols.ClassSymbol;
import cd.parser.JavaliLexer;
import cd.parser.JavaliParser;
import cd.parser.JavaliWalker;
import cd.semantic.TypeSymbolTable;
import cd.semantic.TypedSemanticAnalyzer;
import cd.semantic.UntypedSemanticAnalyzer;

/**
 * The main entrypoint for the compiler. Consists of a series of routines which
 * must be invoked in order. The main() routine here invokes these routines, as
 * does the unit testing code. This is not the <b>best</b> programming practice,
 * as the series of calls to be invoked is duplicated in two places in the code,
 * but it will do for now.
 */
public class Main {

	public static final Logger LOG = LoggerFactory.getLogger(Main.class);

	// Set to non-null to write dump of control flow graph
	public File cfgdumpbase;

	/** Symbol for the Main type */
	public ClassSymbol mainType;

	/** List of all type symbols, used by code generator. */
	public TypeSymbolTable typeSymbols;

	public Main() {

	}

	/** Parse command line, invoke compile() routine */
	public static void main(String args[]) throws IOException {
		Main m = new Main();

		for (String file : args) {
			List<ClassDecl> astRoots;

			// Parse
			try (FileReader fin = new FileReader(file)) {
				astRoots = m.parse(file, fin, false);
			}

			// Run the semantic check
			m.semanticCheck(astRoots);

			// Generate code
			String sFile = file + Config.ASMEXT;
			try (FileWriter fout = new FileWriter(sFile);) {
				m.generateCode(astRoots, fout);
			}
		}
	}

	public List<ClassDecl> parse(Reader file, boolean debugParser)
			throws IOException {
		return parse(null, file, debugParser);
	}

	/**
	 * Parses an input stream into an AST
	 * 
	 * @throws IOException
	 */
	public List<ClassDecl> parse(String fileName, Reader file,
			boolean debugParser) throws IOException {
		List<ClassDecl> result = new ArrayList<>();

		result = parseWithAntlr(fileName, file);
		return result;
	}

	public List<ClassDecl> parseWithAntlr(String file, Reader reader)
			throws IOException {
		try {
			ANTLRReaderStream input = new ANTLRReaderStream(reader);
			JavaliLexer lexer = new JavaliLexer(input);
			CommonTokenStream tokens = new CommonTokenStream(lexer);

			JavaliParser parser = new JavaliParser(tokens);
			JavaliParser.unit_return parserReturn;
			parserReturn = parser.unit();

			CommonTreeNodeStream nodes = new CommonTreeNodeStream(
					parserReturn.getTree());
			nodes.setTokenStream(tokens);

			JavaliWalker walker = new JavaliWalker(nodes);

			LOG.debug("AST Resulting From Parsing Stage:");
			List<ClassDecl> result = walker.unit();
			LOG.debug(AstDump.toString(result));

			return result;
		} catch (RecognitionException e) {
			ParseFailure pf = new ParseFailure(0, "?");
			pf.initCause(e);
			throw pf;
		}
	}

	public void semanticCheck(List<ClassDecl> astRoots) {
		new UntypedSemanticAnalyzer(this).check(astRoots);

		// Uncomment to erase the existing variable types before type inference
		// TODO Should be made configurable
		// GlobalTypeEraser.getInstance().eraseTypesFrom(typeSymbols);
		// LocalTypeEraser.getInstance().eraseTypesFrom(typeSymbols);

		new TypedSemanticAnalyzer(this).check(astRoots);

		// Build control flow graph
		for (ClassDecl cd : astRoots)
			for (MethodDecl md : cd.methods())
				new CFGBuilder(this).build(md);
		CfgDump.toString(astRoots, ".cfg", cfgdumpbase, false);

		// Compute dominators
		for (ClassDecl cd : astRoots)
			for (MethodDecl md : cd.methods())
				new Dominator(this).compute(md);
		CfgDump.toString(astRoots, ".dom", cfgdumpbase, true);

		// Introduce SSA form.
		for (ClassDecl cd : astRoots)
			for (MethodDecl md : cd.methods())
				new SSA(this).compute(md);
		CfgDump.toString(astRoots, ".ssa", cfgdumpbase, false);

		// Optimize using SSA form.
		for (ClassDecl cd : astRoots)
			for (MethodDecl md : cd.methods())
				new Optimizer(this).compute(md);
		CfgDump.toString(astRoots, ".opt", cfgdumpbase, false);

		// Remove SSA form.
		for (ClassDecl cd : astRoots)
			for (MethodDecl md : cd.methods())
				new DeSSA(this).compute(md);
		CfgDump.toString(astRoots, ".dessa", cfgdumpbase, false);
	}

	public void generateCode(List<ClassDecl> astRoots, Writer out) {
		CfgCodeGenerator cg = new CfgCodeGenerator(this, out);
		cg.go(astRoots);
	}

}
