import hr.unizg.fer.zemris.ppj.maheri.lexer.Action;
import hr.unizg.fer.zemris.ppj.maheri.lexer.Lexer;
import hr.unizg.fer.zemris.ppj.maheri.lexer.LexerRule;
import hr.unizg.fer.zemris.ppj.maheri.lexer.LexerState;

/*
 * not used directly, but required for autocompile in grader (and to 
 * deserialize properly
 */
import hr.unizg.fer.zemris.ppj.maheri.lexer.actions.ChangeStateAction;
import hr.unizg.fer.zemris.ppj.maheri.lexer.actions.ComeBackAction;
import hr.unizg.fer.zemris.ppj.maheri.lexer.actions.DeclareClassAction;
import hr.unizg.fer.zemris.ppj.maheri.lexer.actions.NewLineAction;
import hr.unizg.fer.zemris.ppj.maheri.lexer.actions.SkipAction;
import hr.unizg.fer.zemris.ppj.maheri.automaton.Automaton;
import hr.unizg.fer.zemris.ppj.maheri.automaton.State;
import hr.unizg.fer.zemris.ppj.maheri.automaton.eNfa;
import hr.unizg.fer.zemris.ppj.maheri.automaton.Transition;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.Map;
import java.util.Scanner;

@SuppressWarnings("unused")
public class LA {
	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		Map<String, LexerState> lexerStates = null;
		LexerState startState = null;
		try {
			FileInputStream file = new FileInputStream("lexerStates.ser");
			ObjectInputStream oin = new ObjectInputStream(file);
			lexerStates = (Map<String, LexerState>) oin.readObject();
			startState = (LexerState) oin.readObject();
			oin.close();
			file.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		for (LexerState state : lexerStates.values()) {
			System.err.println("name: " + state.getName() + ", with rules [  ");
			for (LexerRule rule : state.getRules()) {
				System.err.println(rule.hashCode() + " for regex  " + rule.realRegex + " with actions : ");
				for (Action a : rule.getActions())
					System.err.println("\t" + a.toString() + ", ");
			}
			System.err.println("] ");
		}

		String input = new Scanner(System.in).useDelimiter("\\A").next();

		new Lexer(lexerStates, startState, input, System.out).doLexing();
	}
}
