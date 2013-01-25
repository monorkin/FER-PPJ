import hr.unizg.fer.zemris.ppj.maheri.parser.LRparser;
import hr.unizg.fer.zemris.ppj.maheri.parser.ParserUtils.ParserTable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class SA {

	public static void main(String[] args) throws IOException {
		run(System.in, System.out, new File(GSA.OUTPUT));
	}

	public static void run(InputStream in, OutputStream out, File parserTable) throws IOException {

		ParserTable table = null;
		try {
			FileInputStream file = new FileInputStream(parserTable);
			ObjectInputStream oin = new ObjectInputStream(file);
			table = (ParserTable) oin.readObject();
			oin.close();
			file.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		table.print();

		List<String> inputLines = new ArrayList<String>();

		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		String currentLine;
		while ((currentLine = reader.readLine()) != null) {
			inputLines.add(currentLine);
		}

		LRparser lrparser = new LRparser(inputLines, table.getActionsTable(), table.getProductions(),
				table.getParserStartState(), table.getSync(), new PrintStream(out));
		lrparser.parse();
	}

}
