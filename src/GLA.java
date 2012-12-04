import hr.unizg.fer.zemris.ppj.maheri.lexer.Action;
import hr.unizg.fer.zemris.ppj.maheri.lexer.LexerRule;
import hr.unizg.fer.zemris.ppj.maheri.lexer.LexerState;
import hr.unizg.fer.zemris.ppj.maheri.lexer.actions.ChangeStateAction;
import hr.unizg.fer.zemris.ppj.maheri.lexer.actions.ComeBackAction;
import hr.unizg.fer.zemris.ppj.maheri.lexer.actions.DeclareClassAction;
import hr.unizg.fer.zemris.ppj.maheri.lexer.actions.NewLineAction;
import hr.unizg.fer.zemris.ppj.maheri.lexer.actions.SkipAction;
import hr.unizg.fer.zemris.ppj.maheri.lexergen.InputProcessor;
import hr.unizg.fer.zemris.ppj.maheri.lexergen.RegexToAutomaton;
import hr.unizg.fer.zemris.ppj.maheri.lexergen.structs.LexerRuleDescriptionText;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
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

		List<String> inputLines = new LinkedList<String>();

		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		String currentLine;
		while ((currentLine = reader.readLine()) != null) {
			inputLines.add(currentLine);
		}

		List<String> regularDefinitionLines;
		List<String> lexerStateNames;
		List<String> tokenNames;
		List<LexerRuleDescriptionText> lexerRuleDesciptions;

		InputProcessor ip = new InputProcessor(inputLines);

		regularDefinitionLines = ip.getRegularDefinitions();

		lexerStateNames = ip.getLexerStates();
		tokenNames = ip.getTokenNames();
		lexerRuleDesciptions = ip.getLexerRules();

		Map<String, LexerState> lexerStates = new HashMap<String, LexerState>();

		for (LexerRuleDescriptionText r : lexerRuleDesciptions) {
			String stateName = r.getActiveStateName();
			if (!lexerStates.containsKey(stateName)) {
				lexerStates.put(stateName, new LexerState(stateName));
			}
			List<Action> ruleActions = new ArrayList<Action>();
			List<String> stringActions = r.getExtraParameterLines();
			String action = r.getActionName();
			if (action.equals("-")) {
				ruleActions.add(new SkipAction());
			} else {
				ruleActions.add(new DeclareClassAction(action));
			}
			for (String s : stringActions) {
				if (s.startsWith("VRATI_SE")) {
					String[] vratiSeAction = s.split(" ");
					int vratiSeZa = Integer.parseInt(vratiSeAction[1]);
					ruleActions.add(0, new ComeBackAction(vratiSeZa));
				} else if (s.startsWith("UDJI_U_STANJE")) {
					String[] udjiStanjeAction = s.split(" ");
					ruleActions.add(new ChangeStateAction(udjiStanjeAction[1]));
				} else if (s.equals("NOVI_REDAK")) {
					ruleActions.add(new NewLineAction());
				}
			}

			LexerRule tmpRule = new LexerRule(RegexToAutomaton.getAutomaton(r.getRegexString()), ruleActions,
					r.getRegexString());
			lexerStates.get(stateName).addRule(tmpRule);
		}

		FileOutputStream stream = new FileOutputStream("analizator/lexerStates.ser");
		ObjectOutputStream oStream = new ObjectOutputStream(stream);
		LexerState startState = lexerStates.get(lexerStateNames.get(0));
		System.err.println("About to begin serialization");
		try {
			oStream.writeObject(lexerStates);
			System.err.println("Wrote other states");
			oStream.writeObject(startState);
			System.err.println("Wrote start state");
			oStream.close();
			stream.close();
		} catch (Error e) {
			System.err.println("Scary error!!!");
			e.printStackTrace();
		}
	}
}
