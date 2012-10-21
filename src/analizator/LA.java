
import hr.unizg.fer.zemris.ppj.maheri.lexer.Lexer;
import hr.unizg.fer.zemris.ppj.maheri.lexer.LexerState;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Map;
import java.util.Scanner;

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
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}

		String input = new Scanner(System.in).useDelimiter("\\A").next();

		new Lexer(lexerStates, startState, input, System.out).doLexing();
	}
}
