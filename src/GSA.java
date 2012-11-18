import hr.unizg.fer.zemris.ppj.maheri.lexer.LexerState;
import hr.unizg.fer.zemris.ppj.maheri.parser.InputProcessor;
import hr.unizg.fer.zemris.ppj.maheri.parser.LRparser;
import hr.unizg.fer.zemris.ppj.maheri.parser.ParserUtils;
import hr.unizg.fer.zemris.ppj.maheri.parser.ParserUtils.ParserTable;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class GSA {
	
	public static void main(String[] args) throws IOException {
		List<String> inputLines = new ArrayList<String>();

		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		String currentLine;
		while ((currentLine = reader.readLine()) != null) {
			inputLines.add(currentLine);
		}
		
		ParserTable table = new ParserUtils(InputProcessor.parseInput(inputLines.toArray(new String[0]))).makeParser();
		
		FileOutputStream stream = new FileOutputStream("analizator/lr1.ser");
		ObjectOutputStream oStream = new ObjectOutputStream(stream);
		System.err.println("About to begin serialization");
		try {
			oStream.writeObject(table);
			System.err.println("Wrote parser table to file");
		} catch (Error e) {
			System.err.println("Scary error!!!");
			e.printStackTrace();
		}
		table.print();
	}
}
