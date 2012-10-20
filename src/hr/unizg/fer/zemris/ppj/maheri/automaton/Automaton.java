package hr.unizg.fer.zemris.ppj.maheri.automaton;

import java.util.List;

/**
 * An abstract class representing an automaton
 * 
 * @author tljubej
 * @author Petar Šegina <psegina@ymail.com>
 * 
 */
public abstract class Automaton {

	/**
	 * A constant marking the symbol used for representing epsilon transitions
	 */
	public static final String EPSILON = "$";

	public Automaton(List<State> states, List<String> symbols, List<Transition> transitions, State startingState,
			List<State> acceptableStates) {

	}

	/**
	 * 
	 * @return false if in a "dead" state, true otherwise
	 */
	abstract public boolean isAlive();

	/**
	 * 
	 * @return true if in an "accepted" state, false otherwise
	 */
	abstract public boolean isAcceptable();

	/**
	 * 
	 * @return the current states of the automaton
	 */
	abstract public List<State> getActiveStates();
	
	/**
	 * Perform a transition based on the input character
	 * 
	 * @param c
	 */
	abstract public void nextChar(String c);

	/**
	 * Resets automaton to starting state
	 */
	public void reset() {

	}

}
