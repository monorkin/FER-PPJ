import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import hr.unizg.fer.zemris.ppj.maheri.lexer.LexerState;
import hr.unizg.fer.zemris.ppj.maheri.parser.LRparser;
import hr.unizg.fer.zemris.ppj.maheri.parser.ParserUtils.ParserTable;


public class SA {
	public static void main(String[] args) throws IOException {
		
		ParserTable table = null;
		try {
			FileInputStream file = new FileInputStream("src/analizator/lr1.ser");
			ObjectInputStream oin = new ObjectInputStream(file);
			table = (ParserTable) oin.readObject();
			oin.close();
			file.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		table.print();
		
		List<String> inputLines = new ArrayList<String>();
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		String currentLine;
		while ((currentLine = reader.readLine()) != null) {
			inputLines.add(currentLine);
		}
		
		LRparser lrparser = new LRparser(inputLines, table.getActionsTable(), table.getProductions(), table.getParserStartState(), table.getSync());
		lrparser.parse();		
	}

}
