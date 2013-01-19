import hr.unizg.fer.zemris.ppj.maheri.semantics.InputProcessor;
import hr.unizg.fer.zemris.ppj.maheri.semantics.Node;
import hr.unizg.fer.zemris.ppj.maheri.semantics.SemanticsAnalyzer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


public class GeneratorKoda {
	
	public static void main(String[] args) throws IOException {
		List<String> inputLines = new ArrayList<String>();

		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		String currentLine;
		while ((currentLine = reader.readLine()) != null) {
			inputLines.add(currentLine);
		}

		InputProcessor ip = new InputProcessor(inputLines);
		Node tree = ip.getTree();
		
		SemanticsAnalyzer semAn = new SemanticsAnalyzer(tree);
		String code = semAn.check().createAsmCode();
		System.out.println(code);
	}

}
