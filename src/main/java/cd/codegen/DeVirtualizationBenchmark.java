package cd.codegen;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.math3.stat.StatUtils;

import cd.CompilationContext;
import cd.CompilerOptions;
import cd.Compiler;
import cd.util.FileUtil;

public class DeVirtualizationBenchmark {

	private static final String JAVALI_FILE = "Benchmark.javali";
	private static final int REPEAT_MEASUREMENT = 100;
	
	private static final int CLASSES = 2000;
	private static final int METHODS_PER_CLASS = 10;
	private static final int ITERATIONS = 100;

	public void generateBenchmarkFile(File javaliOutputFile) throws IOException {
		StringBuilder builder = new StringBuilder();
		
		for (int i = 0; i < CLASSES; i++) {
			builder.append(String.format("class Class%d{\n", i));
			for (int j = 0; j < METHODS_PER_CLASS; j++) {
				builder.append(String.format("void m%d(){ }\n", j));
			}
			builder.append("}\n\n");
		}
		
		builder.append("class Main {\n");
		
		// keep all instances in fields
		for (int i = 0; i < CLASSES; i++) {
			builder.append(String.format("Class%d c%d;\n", i, i));
		}
		
		// method to create objects
		builder.append("void init() {\n");
		// keep all instances in fields
		for (int i = 0; i < CLASSES; i++) {
			builder.append(String.format("c%d = new Class%d();\n", i, i));
		}
		builder.append("}\n");
		
		// calling all the methods
		builder.append("void callAll() {\n");
		for (int i = 0; i < METHODS_PER_CLASS; i++) {
			for (int j = 0; j < CLASSES; j++) {
				builder.append(String.format("c%d.m%d();\n", j,i));
			}
		}
		builder.append("}\n");
		
		builder.append(String.format(
				"	void main() {\n" + 
				"		int ITERATIONS,i;\n" + 
				"		\n" + 
				"		ITERATIONS = %d;\n" + 
				"		i = 0;\n" + 
				"		init();\n" +
				"		tick();\n" + 
				"		while (i < ITERATIONS) {\n" + 
				"			callAll();\n" + 
				"			i = i+1;\n" + 
				"		}\n" + 
				"		tock();\n" + 
				"		\n" + 
				"	}\n", ITERATIONS));
		
		
		
		builder.append("}");
		
		FileUtils.writeStringToFile(javaliOutputFile, builder.toString());
	}
	
	private double measureAvgExecutionTime(File binaryFile) throws IOException {
		double[] tscTimes = new double[REPEAT_MEASUREMENT];
		for (int i = 0; i < REPEAT_MEASUREMENT; i++) {
			String execOut = FileUtil.runCommand(new File("."),
					new String[] { binaryFile.getAbsolutePath() }, new String[] {}, null,
					true);
			assert(!execOut.contains("Error"));
			tscTimes[i] = Double.valueOf(execOut);
		}
		return StatUtils.mean(tscTimes);
	}

	public void run() {
		try {
			File benchmarkJavaliSource = new File(JAVALI_FILE);
			generateBenchmarkFile(benchmarkJavaliSource);
			
			CompilerOptions options1 = new CompilerOptions();
			options1.setDevirtualizing(false);
			CompilationContext context1 = new CompilationContext(benchmarkJavaliSource, options1);
			Compiler compiler1 = Compiler.forContext(context1);
			
			CompilerOptions options2 = new CompilerOptions();
			options2.setDevirtualizing(true);
			CompilationContext context2 = new CompilationContext(benchmarkJavaliSource, options2);
			Compiler compiler2 = Compiler.forContext(context2);
			
			compiler1.compile();
			double unoptimizedRuntimeAvg = measureAvgExecutionTime(context1.getBinaryFile());
	
			compiler2.compile();
			double optimizedRuntimeAvg = measureAvgExecutionTime(context2.getBinaryFile());
			
			double savedCyclesPercent = 100.0*(unoptimizedRuntimeAvg - optimizedRuntimeAvg)/ unoptimizedRuntimeAvg;
			String benchmarkResult = String.format(
					"Unoptimized code took %d, optimized code %d cycles, saved %.2f%%",
					(long) unoptimizedRuntimeAvg, (long) optimizedRuntimeAvg, savedCyclesPercent);
			System.out.println(benchmarkResult);
			
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public static void main(String[] args) {
		DeVirtualizationBenchmark benchmark = new DeVirtualizationBenchmark();
		benchmark.run();

	}

}
