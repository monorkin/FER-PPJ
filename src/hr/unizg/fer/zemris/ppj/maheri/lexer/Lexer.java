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
		// mislim da ce ti trebati i treci pokazivac. Na slajdovima koriste tri
		// pokazivaca. Mozda ti ne treba ovdje ovisno dal ce taj treci biti neki
		// i ili j u nekoj for petljiNETREBA PUŠI KURACIPAK TREBA
		startIndex = 0;
		finishIndex = 0;
		lastIndex=-1;
		lineCount = 1;
	}

	/**
	 * Starts (and hopefully finishes) the lexical analysis
	 * 
	 * @param input
	 *            the entire file to be lexerized
	 */
	public void doLexing() {
		LexerRule accRule=null;
		
		while (finishIndex<=input.length()) {
			char c = input.charAt(finishIndex);
			currentState.pushCharToAutomatons(c);
			LexerRule tmpRule = currentState.getAccepted();
			if (tmpRule!=null) {
				accRule=tmpRule;
				lastIndex=finishIndex;
			}
			if (currentState.isAnyAlive() && finishIndex!=input.length()) {
				finishIndex++;
			} else {
				if (accRule==null) {
					System.err.println("LEL ERROR U LEXERANJU");
					startIndex++;
					lastIndex=-1;
					finishIndex=startIndex;
				} else {
					finishIndex=lastIndex+1;
					accRule.doActions(this);
					lastIndex=-1;
					accRule=null;
				}
			}
		}
		
		//OPET NEVALJA
//		while (finishIndex < input.length()) {
//			char currChar = input.charAt(finishIndex);
//			currentState.pushCharToAutomatons(currChar);
//			if (currentState.isAnyAlive()) {
//				finishIndex++;
//			} else {
//				currentState.revertAutomatons();
//				LexerRule accRule = currentState.getAccepted();
//				if (accRule == null) {
//					startIndex++;
//					finishIndex = startIndex;
//					System.err.println("LEL POGRIJEŠKA U LEXORIRANJU");
//				} else {
//					accRule.doActions(this);
//				}
//				currentState.resetAutomatons();
//
//			}
//		}
		// Ovo možda ne valja
		// for (int i = startIndex; i < finishIndex; i++) {
		// char currChar = input.charAt(i);
		// if (currentState.isAnyAlive()) {
		// finishIndex++;
		// continue;
		// } else {
		// LexerRule accepted = currentState.getAccepted();
		// if (accepted == null) {
		// startIndex++;
		// i = startIndex;
		// }
		// }
		// }
	}

	/**
	 * @return the lastIndex
	 */
	public int getLastIndex() {
		return lastIndex;
	}

	/**
	 * @param lastIndex the lastIndex to set
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
