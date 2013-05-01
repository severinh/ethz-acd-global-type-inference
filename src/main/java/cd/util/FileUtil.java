package cd.util;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.apache.commons.io.IOUtils;

public class FileUtil {

	public static String runCommand(File dir, String[] command,
			String[] substs, String input, boolean detectError)
			throws IOException {
		// Substitute the substitution strings $0, $1, etc
		String newCommand[] = new String[command.length];
		for (int i = 0; i < command.length; i++) {
			String newItem = command[i];
			for (int j = 0; j < substs.length; j++)
				newItem = newItem.replace("$" + j, substs[j]);
			newCommand[i] = newItem;
		}

		// Run the command in the specified directory
		ProcessBuilder pb = new ProcessBuilder(newCommand);
		pb.directory(dir);
		pb.redirectErrorStream(true);
		Process p = pb.start();
		if (input != null && !input.equals("")) {
			try (OutputStreamWriter osw = new OutputStreamWriter(
					p.getOutputStream())) {
				osw.write(input);
			}
		}
		String result = IOUtils.toString(p.getInputStream(), "UTF-8");

		if (detectError) {
			int err;
			try {
				err = p.waitFor();

				// hack: same as ReferenceServer returns when
				// a dynamic error occurs running the interpreter
				if (err != 0)
					return "Error: " + err + "\n";
			} catch (InterruptedException e) {
			}
		}
		return result;
	}

}
