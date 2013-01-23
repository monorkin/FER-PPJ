import hr.unizg.fer.zemris.ppj.maheri.semantics.InputProcessor;
import hr.unizg.fer.zemris.ppj.maheri.semantics.Node;
import hr.unizg.fer.zemris.ppj.maheri.semantics.SemanticsAnalyzer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class GeneratorKoda {

	public static void main(String[] args) throws IOException {
		run(System.in, System.out);
	}

	public static void run(InputStream in, OutputStream out) throws IOException {
		List<String> inputLines = new ArrayList<String>();

		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		String currentLine;
		while ((currentLine = reader.readLine()) != null) {
			inputLines.add(currentLine);
		}

		InputProcessor ip = new InputProcessor(inputLines);
		Node tree = ip.getTree();

		SemanticsAnalyzer semAn = new SemanticsAnalyzer(tree);
		String code = semAn.check().createAsmCode();
		// code =
		// "\t`BASE D\r\nstart\tMOVE %H 40000, R7\r\n\tCALL GLOBAL_INITIALIZERS\r\n\tCALL GLOBAL_main\r\n\tHALT\r\nGLOBAL_main\tPUSH R5\r\n\tMOVE 71, R1\r\n\tPUSH R1\r\n\tPOP R6\r\n\tJP RET_FROM_main\r\nRET_FROM_main\tPOP R5\r\n\tRET\r\nGLOBAL_INITIALIZERS\tRET\r\n";

		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("a.frisc")));
		writer.write(code);
		writer.close();

		new PrintWriter(out).println(code);
	}

}
