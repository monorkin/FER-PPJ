package hr.unizg.fer.zemris.ppj.maheri.lexer;

import java.io.PrintStream;
import java.util.Map;

/**
 * This does the actual fucking lexical analysis. Outputs results as specified
 * by ppj-lab1.pdf to output.
 * 
 * @author tljubej
 * 
 */
public class Lexer {
	private Map<String, LexerState> lexerStates;
	private LexerState currentState;
	private String input;
	private PrintStream output;
	private int startIndex, finishIndex, lastIndex, lineCount;

	/**
	 * @param lexerStates
	 * @param startState
	 * @param input
	 */
	public Lexer(Map<String, LexerState> lexerStates, LexerState startState, String input, PrintStream output) {
		this.lexerStates = lexerStates;
		this.currentState = startState;
		this.input = input;
		this.output = output;
		
		startIndex = 0;
		finishIndex = -1;
		lastIndex = -1;
		lineCount = 1;

		System.err.println("Made lexer starting in state " + startState.getName());
	}

	/**
	 * Starts (and hopefully finishes) the lexical analysis
	 * 
	 * @param input
	 *            the entire file to be lexerized
	 */
	public void doLexing() {
		LexerRule accRule = null;

		while (finishIndex < input.length()-1) {
//			System.err.println("----------\n[" + currentState.getName() + ">");
			while (currentState.isAnyAlive()) {
				++finishIndex;
//				System.err.println("So far processed [" + input.substring(startIndex, finishIndex) + "]" + ", s="
//						+ startIndex + " f=" + finishIndex + " l=" + lastIndex);
				LexerRule tmpRule = currentState.getAccepted();
				if (tmpRule == null) {
//					System.err.println("Some rules left, but none accept");
				} else {
//					System.err.println("Accepted rule " + tmpRule.hashCode() + " for regex  " + tmpRule.realRegex);
					accRule = tmpRule;
					lastIndex = finishIndex - 1;
				}
				if (finishIndex < input.length()) {
					currentState.pushCharToAutomatons(String.valueOf(input.charAt(finishIndex)));
				} else {
					break;
				}
			}
			
//			if (finishIndex == input.length()) {
//				System.err.println("EOF found. left with " + input.substring(startIndex));
//			} else {
//				System.err.println("No rules left, gone too far with [" + input.substring(startIndex, finishIndex + 1)
//						+ "]" + ", s=" + startIndex + " f=" + finishIndex + " l=" + lastIndex);
//				System.err.println("Last good is ["
//						+ (lastIndex >= startIndex ? input.substring(startIndex, lastIndex + 1) : "") + "]");
//			}
			
			if (accRule == null) {
//				System.err.println("no accepted rules = lexing error at line " + lineCount
//						+ ", ignoring invalid character '" + input.charAt(startIndex) + "' at pos " + startIndex);
				finishIndex = startIndex++;
				currentState.resetAutomatons();
			} else {
//				System.err.println("selecting accepted rule, rule " + accRule.hashCode() + " for regex  "
//						+ accRule.realRegex);
				finishIndex = lastIndex;
				accRule.doActions(this);
				accRule = null;
				currentState.resetAutomatons();
			}
		}
	}

	/**
	 * @return the lastIndex
	 */
	public int getLastIndex() {
		return lastIndex;
	}

	/**
	 * @param lastIndex
	 *            the lastIndex to set
	 */
	public void setLastIndex(int lastIndex) {
		this.lastIndex = lastIndex;
	}

	/**
	 * @return the output
	 */
	public PrintStream getOutput() {
		return output;
	}

	/**
	 * @param currentState
	 *            the currentState to set
	 */
	public void setCurrentState(LexerState currentState) {
		this.currentState = currentState;
	}

	public void incrementLineCount() {
		lineCount++;
	}

	/**
	 * @return the input
	 */
	public String getInput() {
		return input;
	}

	/**
	 * @param input
	 *            the input to set
	 */
	public void setInput(String input) {
		this.input = input;
	}

	/**
	 * @return the startIndex
	 */
	public int getStartIndex() {
		return startIndex;
	}

	/**
	 * @param startIndex
	 *            the startIndex to set
	 */
	public void setStartIndex(int startIndex) {
		this.startIndex = startIndex;
	}

	/**
	 * @return the finishIndex
	 */
	public int getFinishIndex() {
		return finishIndex;
	}

	/**
	 * @param finishIndex
	 *            the finishIndex to set
	 */
	public void setFinishIndex(int finishIndex) {
		this.finishIndex = finishIndex;
	}

	/**
	 * @return the lexerStates
	 */
	public Map<String, LexerState> getLexerStates() {
		return lexerStates;
	}

	/**
	 * @return the currentState
	 */
	public LexerState getCurrentState() {
		return currentState;
	}

	/**
	 * @return the lineCount
	 */
	public int getLineCount() {
		return lineCount;
	}

	public void reset() {
		startIndex = 0;
		finishIndex = 0;
		lineCount = 1;
	}

}
