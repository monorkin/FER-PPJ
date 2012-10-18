import hr.unizg.fer.zemris.ppj.maheri.gen.interfaces.IInputProcessor;
import hr.unizg.fer.zemris.ppj.maheri.gen.interfaces.RegexParser;
import hr.unizg.fer.zemris.ppj.maheri.lexer.gen.structs.LexerRuleDescriptionText;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * The supposed entry point for the automated submission grader, as outlined in
 * the problem statement.
 * 
 * @author dosvald
 * 
 */
public class GLA {

	public static void main(String[] args) throws IOException {
		// Kad main postane velik, moze se grupirati u metode ili konstruktore nekih klasa koje ce isplivati kao potrebne... 
		
		List<String> inputLines = new LinkedList<String>();

		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		String currentLine;
		while ((currentLine = reader.readLine()) != null) {
			inputLines.add(currentLine);
		}

		List<String> regularDefinitionLines;		
		List<String> lexerStateNames;
		List<String> tokenNames;
		List<LexerRuleDescriptionText> lexerRules;
		
		// TODO implement where error
		IInputProcessor ip = new SimpleInputProcessor(inputLines);
		
		regularDefinitionLines = ip.getRegularDefinitions();

		Map<String, String> regularDefinitions;
		RegexParser regDefParser = new SimpleRegexParser();
		regularDefinitions = regDefParser.parseRegexes(regularDefinitionLines.toArray(new String[0]));
		
		lexerStateNames = ip.getLexerStates();
		tokenNames = ip.getTokenNames();
		lexerRules = ip.getLexerRules(regularDefinitions);
		
		
		
	}
}
