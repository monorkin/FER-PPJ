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
			System.err.println("----------\n[" + currentState.getName() + ">");
			while (currentState.isAnyAlive()) {
				++finishIndex;
				System.err.println("So far processed [" + input.substring(startIndex, finishIndex) + "]" + ", s="
						+ startIndex + " f=" + finishIndex + " l=" + lastIndex);
				LexerRule tmpRule = currentState.getAccepted();
				if (tmpRule == null) {
					System.err.println("Some rules left, but none accept");
				} else {
					System.err.println("Accepted rule " + tmpRule.hashCode() + " for regex  " + tmpRule.realRegex);
					accRule = tmpRule;
					lastIndex = finishIndex - 1;
				}
				if (finishIndex < input.length()) {
					currentState.pushCharToAutomatons(String.valueOf(input.charAt(finishIndex)));
				} else {
					break;
				}
			}
			
			if (finishIndex == input.length()) {
				System.err.println("EOF found. left with " + input.substring(startIndex));
			} else {
				System.err.println("No rules left, gone too far with [" + input.substring(startIndex, finishIndex + 1)
						+ "]" + ", s=" + startIndex + " f=" + finishIndex + " l=" + lastIndex);
				System.err.println("Last good is ["
						+ (lastIndex >= startIndex ? input.substring(startIndex, lastIndex + 1) : "") + "]");
			}
			
			if (accRule == null) {
				System.err.println("no accepted rules = lexing error at line " + lineCount
						+ ", ignoring invalid character '" + input.charAt(startIndex) + "' at pos " + startIndex);
				finishIndex = ++startIndex;
				currentState.resetAutomatons();
			} else {
				System.err.println("selecting accepted rule, rule " + accRule.hashCode() + " for regex  "
						+ accRule.realRegex);
				finishIndex = lastIndex;
				accRule.doActions(this);
				accRule = null;
				currentState.resetAutomatons();
			}
			//
			// while (finishIndex < input.length() - 1) {
			// System.err.print("[" + currentState.getName() + "], s=" +
			// startIndex + " f=" + finishIndex + " l="
			// + lastIndex);
			// LexerRule tmpRule = currentState.getAccepted();
			// if (tmpRule == null && currentState.isAnyAlive()) {
			// System.err.println("Some rules left, but none accept");
			// char c = input.charAt(finishIndex);
			// System.err.println("[Read '" + c + "']@" + finishIndex);
			// ++finishIndex;
			// currentState.pushCharToAutomatons(String.valueOf(c));
			// } else if (tmpRule != null) {
			// System.err.println("Rule " + tmpRule.hashCode() + " for regex  "
			// + tmpRule.realRegex + " accepts");
			// accRule = tmpRule;
			// lastIndex = finishIndex;
			// char c = input.charAt(finishIndex);
			// System.err.println("[Read '" + c + "']@" + finishIndex);
			// ++finishIndex;
			// currentState.pushCharToAutomatons(String.valueOf(c));
			// } else if (!currentState.isAnyAlive()) {
			// System.err.println("No rules left");
			// if (accRule == null) {
			// System.err.println("Lexing error at line " + lineCount +
			// ", invalid character "
			// + input.charAt(startIndex));
			// finishIndex = startIndex++;
			// currentState.resetAutomatons();
			// } else {
			// System.err.println("Selecting last accepted rule, rule " +
			// accRule.hashCode() + " for regex  "
			// + accRule.realRegex);
			// finishIndex = lastIndex;
			// accRule.doActions(this);
			// accRule = null;
			// currentState.resetAutomatons();
			// }
			// }

			// System.err.printf("in state %s\t [%s]%c\n",
			// currentState.getName(), input.substring(startIndex,
			// lastIndex>=startIndex?lastIndex:startIndex+1), c);
			// currentState.pushCharToAutomatons(String.valueOf(c));
			// if (tmpRule!=null) {
			// System.err.println("Rule " + tmpRule.hashCode() + " for regex  "
			// + tmpRule.realRegex + " accepts");
			// accRule=tmpRule;
			// lastIndex=finishIndex;
			// }
			// if (currentState.isAnyAlive() && finishIndex!=input.length()) {
			// System.err.println("Some rules may still match");
			// finishIndex++;
			// } else {
			// System.err.println("No rule match is alive");
			// if (accRule==null) {
			// System.err.println("LEL ERROR U LEXERANJU");
			// startIndex++;
			// lastIndex=-1;
			// finishIndex=startIndex;
			// } else {
			// System.err.println("Doing acion for last accepted rule, that is "
			// + "Rule " + accRule.hashCode() + " for regex  " +
			// accRule.realRegex);
			// finishIndex=lastIndex+1;
			// accRule.doActions(this);
			// lastIndex=-1;
			// accRule=null;
			// }
			// currentState.resetAutomatons();
			// }
		}

		// OPET NEVALJA
		// while (finishIndex < input.length()) {
		// char currChar = input.charAt(finishIndex);
		// currentState.pushCharToAutomatons(currChar);
		// if (currentState.isAnyAlive()) {
		// finishIndex++;
		// } else {
		// currentState.revertAutomatons();
		// LexerRule accRule = currentState.getAccepted();
		// if (accRule == null) {
		// startIndex++;
		// finishIndex = startIndex;
		// System.err.println("LEL POGRIJEŠKA U LEXORIRANJU");
		// } else {
		// accRule.doActions(this);
		// }
		// currentState.resetAutomatons();
		//
		// }
		// }
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
