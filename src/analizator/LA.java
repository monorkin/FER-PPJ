import hr.unizg.fer.zemris.ppj.maheri.Logger;
import hr.unizg.fer.zemris.ppj.maheri.lexer.Action;
import hr.unizg.fer.zemris.ppj.maheri.lexer.Lexer;
import hr.unizg.fer.zemris.ppj.maheri.lexer.LexerRule;
import hr.unizg.fer.zemris.ppj.maheri.lexer.LexerState;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Map;
import java.util.Scanner;

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

@SuppressWarnings("unused")
public class LA {

	public static void main(String[] args) {
		run(System.in, System.out, new File("lexerStates.ser"));
	}

	@SuppressWarnings("unchecked")
	public static void run(InputStream in, OutputStream out, File lexerStatesFile) {
		Map<String, LexerState> lexerStates = null;
		LexerState startState = null;
		try {
			FileInputStream file = new FileInputStream(lexerStatesFile);
			ObjectInputStream oin = new ObjectInputStream(file);
			lexerStates = (Map<String, LexerState>) oin.readObject();
			startState = (LexerState) oin.readObject();
			oin.close();
			file.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		for (LexerState state : lexerStates.values()) {
			Logger.log("name: " + state.getName() + ", with rules [  ");
			for (LexerRule rule : state.getRules()) {
				Logger.log(rule.hashCode() + " for regex  " + rule.realRegex + " with actions : ");
				for (Action a : rule.getActions())
					Logger.log("\t" + a.toString() + ", ");
			}
			Logger.log("] ");
		}

		String input = new Scanner(in).useDelimiter("\\A").next();

		new Lexer(lexerStates, startState, input, new PrintStream(out)).doLexing();
	}
}
