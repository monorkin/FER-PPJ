package hr.unizg.fer.zemris.ppj.maheri.automaton;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class State implements Comparable<State>, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2135739431782216137L;
	
	private Object data;
	
	public Object getData() {
		return data;
	}
	
	public void setData(Object data) {
		this.data = data;
	}

	protected String name;
	protected List<Transition> transitions = new LinkedList<Transition>();;
	protected Transition eTransition = null;

	public State(String name) {
		this.name = name;
	}

	public boolean hasEpsilonTransition() {
		return eTransition != null;
	}

	public Transition getEpsilonTransition() {
		return this.eTransition;
	}

	public static State getByName(String stateName, List<State> stateList) {
		for (State s : stateList) {
			if (s.equals(stateName)) {
				return s;
			}
		}
		return null;
	}

	@Override
	public int compareTo(State arg0) {
		return this.name.compareTo(arg0.name);
	}

	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}

	@Override
	public boolean equals(Object arg0) {
		if (arg0 instanceof String) {
			return this.name.equals(arg0);
		}
		if (arg0 instanceof State) {
			return this.name.equals(((State) arg0).name);
		}
		return false;
	}

	@Override
	public String toString() {
		return this.name;
	}

}
