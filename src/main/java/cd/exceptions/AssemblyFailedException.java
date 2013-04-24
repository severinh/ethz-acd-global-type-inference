package cd.exceptions;

public class AssemblyFailedException extends RuntimeException {

	private static final long serialVersionUID = -5658502514441032016L;

	private final String assemblerOutput;

	public AssemblyFailedException(String assemblerOutput) {
		super("Executing assembler failed.\n" + "Output:\n" + assemblerOutput);
		this.assemblerOutput = assemblerOutput;
	}

	public String getAssemblerOutput() {
		return assemblerOutput;
	}

}
