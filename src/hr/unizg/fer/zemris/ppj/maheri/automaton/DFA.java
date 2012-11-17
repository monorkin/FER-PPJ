package hr.unizg.fer.zemris.ppj.maheri.automaton;

import hr.unizg.fer.zemris.ppj.maheri.Logger;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class DFA {
	private HashMap<State, HashMap<String, Transition>> allInOne = new HashMap<State, HashMap<String, Transition> >();
	
	private State start;
	private Set<State> acceptables;
	
	private State current;
	
// interface Automaton ima hrpu nekih lista koje smetaju samo, pa ga ovo ne implementira
	
	public DFA(Collection<State> states, Collection<Transition> transitions, Collection<State> acceptables, State start) {
		Set<State> statesSet = new HashSet<State>(states);
		Set<Transition> transitionsSet = new HashSet<Transition>(transitions);
		
		this.acceptables = new HashSet<State>(acceptables);
		
		for (State state : statesSet) {
			HashMap<String, Transition> map = new HashMap<String, Transition>();
			allInOne.put(state, map);
		}
		
		for (State state : statesSet) {
			HashMap<String, Transition> map = allInOne.get(state);
			
			for (Transition tran : transitionsSet) {
				if (tran == null)
					throw new IllegalArgumentException();
				if (tran.getOrigin() != state)
					continue;
				String key = tran.getKey();
				if (key == null)
					throw new IllegalArgumentException("null key in transition");
				map.put(key, tran);
			}
		}
		
		this.start = start;
		current = start;
	}
	
	public void nextChar(String s) {
		Logger.log("Currently in " + current);
		if (current == null)
			return;
		if (Automaton.EPSILON.equals(s)) {
			throw new IllegalArgumentException("No can do epsilons");
		}
		HashMap<String, Transition> forState = allInOne.get(current);
		if (forState == null)
			throw new IllegalStateException("Ended up in wrong state");
		Transition transition = forState.get(s);
		if (transition == null) {
			current = null;
			return;
		}
		State destination = transition.getDestination();
		Logger.log("Ended up in " + destination);
		Logger.log("via " + transition);
		current = destination;
	}
	
	public boolean isAlive() {
		return current != null;
	}
	
	public boolean isAcceptable() {
		return acceptables.contains(current);
	}
	
	public void reset() {
		current = start;
	}

	public State getActiveState() {
		return current;
	}

}
