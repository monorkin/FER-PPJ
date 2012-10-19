package hr.unizg.fer.zemris.ppj.maheri.automaton;

import java.util.List;

/**
 * Implementation of e-NFA
 * 
 * @author tljubej
 * 
 */
public class Automaton {
	// TODO sve više manje
	private List<String> automatonDescription;
	
	public Automaton(List<String> rules) {
		this.automatonDescription=rules;
		//TODO vjerojatno će tu još nešto ić
	}

	/**
	 * 
	 * @return false if in a "dead" state, true otherwise
	 */
	public boolean isAlive() {
		return false;
	}

	/**
	 * 
	 * @return true if in an "accepted" state, false otherwise
	 */
	public boolean doesAccept() {
		return false;
	}

	public void nextChar(char c) {
	}
	
	/**
	 * Resets automaton to starting state
	 */
	public void reset() {
		
	}

}
