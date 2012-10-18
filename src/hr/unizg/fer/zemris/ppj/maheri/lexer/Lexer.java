package hr.unizg.fer.zemris.ppj.maheri.lexer;

import java.io.PrintStream;
import java.util.Map;

/**
 * This does the actual fucking lexical analysis. Outputs results as specified
 * by ppj-lab1.pdf to outputStream.
 * 
 * @author tljubej
 * 
 */
public class Lexer {
	private Map<String, LexerState> lexerStates;
	private LexerState currentState;
	private PrintStream outputStream;

	/**
	 * @param lexerStates
	 * @param startState
	 * @param outputStream
	 */
	public Lexer(Map<String, LexerState> lexerStates, LexerState startState, PrintStream outputStream) {
		this.lexerStates = lexerStates;
		this.currentState = startState;
		this.outputStream = outputStream;
	}

	/**
	 * Start the lexical analysis
	 * 
	 * @param input
	 *            the entire file to be lexerized
	 */
	public void doLexing(String input) {

	}

}
