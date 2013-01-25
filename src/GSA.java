import hr.unizg.fer.zemris.ppj.maheri.Logger;
import hr.unizg.fer.zemris.ppj.maheri.parser.InputProcessor;
import hr.unizg.fer.zemris.ppj.maheri.parser.ParserUtils;
import hr.unizg.fer.zemris.ppj.maheri.parser.ParserUtils.ParserTable;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class GSA {

	public static final String OUTPUT = "analizator/lr1.ser";
	
	public static void main(String[] args) throws IOException {
		run(System.in);
	}

	public static void run(InputStream in) throws IOException {
		List<String> inputLines = new ArrayList<String>();

		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		String currentLine;
		while ((currentLine = reader.readLine()) != null) {
			inputLines.add(currentLine);
		}

		ParserTable table = new ParserUtils(InputProcessor.parseInput(inputLines.toArray(new String[0]))).makeParser();

		FileOutputStream stream = new FileOutputStream(OUTPUT);
		ObjectOutputStream oStream = new ObjectOutputStream(stream);
		Logger.log("About to begin serialization");
		try {
			oStream.writeObject(table);
			Logger.log("Wrote parser table to file");
		} catch (Error e) {
			Logger.log("Scary error!!!");
			e.printStackTrace();
		}
		table.print();
	}
}
