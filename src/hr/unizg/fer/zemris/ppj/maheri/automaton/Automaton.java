package hr.unizg.fer.zemris.ppj.maheri.automaton;

import java.io.Serializable;
import java.util.List;

/**
 * An abstract class representing an automaton
 * 
 * @author tljubej
 * @author Petar Segina <psegina@ymail.com>
 * 
 */
public abstract class Automaton implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6344259345535148906L;

	/**
	 * A constant marking the symbol used for representing epsilon transitions
	 */
	public static final String EPSILON = "";

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
	 * @return A list of all symbols valid for this automaton
	 */
	abstract public List<String> getSymbols();

	/**
	 * 
	 * @return The starting state of this automaton
	 */
	abstract public State getStartState();

	/**
	 * 
	 * @return A list of acceptable states for this automaton
	 */
	abstract public List<State> getAcceptableStates();

	/**
	 * 
	 * @return A list of states of this automaton
	 */
	abstract public List<State> getStates();

	/**
	 * 
	 * @return A list of the transitions which are a part of this automaton
	 */
	abstract public List<Transition> getTransitions();

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
	public abstract void reset();

}
