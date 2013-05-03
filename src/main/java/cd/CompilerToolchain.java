package cd;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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
import cd.exceptions.AssemblyFailedException;
import cd.exceptions.ParseFailure;
import cd.ir.ast.ClassDecl;
import cd.ir.ast.MethodDecl;
import cd.parser.JavaliLexer;
import cd.parser.JavaliParser;
import cd.parser.JavaliWalker;
import cd.semantic.TypedSemanticAnalyzer;
import cd.semantic.UntypedSemanticAnalyzer;
import cd.semantic.ti.TypeEraser;
import cd.semantic.ti.TypeInference;
import cd.util.FileUtil;

public class CompilerToolchain {
	public static final Logger LOG = LoggerFactory
			.getLogger(CompilerToolchain.class);

	private final CompilationContext context;

	protected CompilerToolchain(CompilationContext context) {
		this.context = context;
	}

	public static CompilerToolchain forContext(CompilationContext context) {
		return new CompilerToolchain(context);
	}

	public void compile() throws IOException {
		parse();
		semanticCheck();
		generateCode();
		assembleExecutable();
	}

	public void parse() throws IOException {
		try {
			try (FileReader fin = new FileReader(context.getSourceFile())) {
				ANTLRReaderStream input = new ANTLRReaderStream(fin);
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

				context.setAstRoots(result);
			}
		} catch (RecognitionException e) {
			ParseFailure pf = new ParseFailure(0, "?");
			pf.initCause(e);
			throw pf;
		}
	}

	public void semanticCheck() {
		List<ClassDecl> astRoots = context.getAstRoots();
		new UntypedSemanticAnalyzer(context).check(astRoots);

		CompilerOptions options = context.getOptions();

		TypeErasureMode typeEraserMode = options.getTypeErasureMode();
		TypeEraser typeEraser = typeEraserMode.getTypeEraser();
		typeEraser.eraseTypesFrom(context.getTypeSymbols());

		TypeInferenceMode typeInferenceMode = options.getTypeInferenceMode();
		TypeInference typeInference = typeInferenceMode.getTypeInference();
		typeInference.inferTypes(context);

		new TypedSemanticAnalyzer(context).check(astRoots);

		File cfgDumpBase = context.getCfgDumpBase();

		// Build control flow graph
		for (ClassDecl cd : astRoots)
			for (MethodDecl md : cd.methods())
				new CFGBuilder().build(md);
		CfgDump.toString(astRoots, ".cfg", cfgDumpBase, false);

		// Compute dominators
		for (ClassDecl cd : astRoots)
			for (MethodDecl md : cd.methods())
				new Dominator().compute(md);
		CfgDump.toString(astRoots, ".dom", cfgDumpBase, true);

		// Introduce SSA form.
		for (ClassDecl cd : astRoots)
			for (MethodDecl md : cd.methods())
				new SSA(context).compute(md);
		CfgDump.toString(astRoots, ".ssa", cfgDumpBase, false);

		// Optimize using SSA form.
		for (ClassDecl cd : astRoots)
			for (MethodDecl md : cd.methods())
				new Optimizer(context).compute(md);
		CfgDump.toString(astRoots, ".opt", cfgDumpBase, false);

		// Remove SSA form.
		for (ClassDecl cd : astRoots)
			for (MethodDecl md : cd.methods())
				new DeSSA().compute(md);
		CfgDump.toString(astRoots, ".dessa", cfgDumpBase, false);
	}

	public void generateCode() throws IOException {
		try (FileWriter fout = new FileWriter(context.getAssemblyFile())) {
			CfgCodeGenerator cg = new CfgCodeGenerator(context, fout);
			cg.go(context.getAstRoots());
		}
	}

	public void assembleExecutable() throws IOException {
		// At this point, we have generated a .s file and we have to compile
		// it to a binary file. We need to call out to GCC or something
		// to do this.
		String binaryFilePath = context.getBinaryFile().getAbsolutePath();
		String assemblyFilePath = context.getAssemblyFile().getAbsolutePath();
		String asmOutput = FileUtil.runCommand(Config.ASM_DIR, Config.ASM,
				new String[] { binaryFilePath, assemblyFilePath }, null, false);

		// To check if gcc succeeded, check if the binary file exists.
		// We could use the return code instead, but this seems more
		// portable to other compilers / make systems.
		if (!context.getBinaryFile().exists())
			throw new AssemblyFailedException(asmOutput);
	}
}
