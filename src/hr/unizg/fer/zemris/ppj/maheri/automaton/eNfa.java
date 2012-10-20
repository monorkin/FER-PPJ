package hr.unizg.fer.zemris.ppj.maheri.automaton;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

/**
 * A representation of a Non-deterministic finite automaton with epsilon
 * transitions
 * 
 * @author Petar Šegina <psegina@ymail.com>
 * 
 */
public class eNfa extends Automaton {

	private List<State> states;
	private List<String> symbols;
	private List<Transition> transitions;
	private State startingState;
	private List<State> acceptableStates;

	private List<State> epsilonInstances = new LinkedList<>();

	/*
	 * currentStates is a Set containing all the states in the current iteration
	 * of the simulation tree
	 */
	private List<State> currentStates = new LinkedList<>();
	/*
	 * newStates is a Set containing all the states which will appear in the
	 * next iteration of the simulation tree
	 */
	private List<State> nextStates = new LinkedList<>();

	private static final boolean DEBUG = false;

	public eNfa(List<State> states, List<String> symbols, List<Transition> transitions, State startingState,
			List<State> acceptableStates) {
		super(states, symbols, transitions, startingState, acceptableStates);
		this.states = states;
		this.symbols = symbols;
		this.transitions = transitions;
		this.startingState = startingState;
		this.acceptableStates = acceptableStates;

		for (Transition t : this.transitions) {
			if (t.getKey().equals(EPSILON)) {
				this.epsilonInstances.add(t.getOrigin());
				t.getOrigin().eTransition = t;
			}

			if (!this.states.contains(t.getOrigin()) || !this.states.containsAll(t.getDestinations())) {
				StringBuilder sb = new StringBuilder();
				sb.append("Transition contains a state not present in the state list!\nTransition:\n\t");
				sb.append(t);
				sb.append("\nState list:\n");
				for (State s : states) {
					sb.append("\t");
					sb.append(s);
					sb.append("\n");
				}
				throw new IllegalArgumentException(sb.toString());
			}

		}

		minimise();
		initialise();
	}

	@Override
	public boolean isAlive() {
		return !currentStates.isEmpty();
	}

	@Override
	public boolean isAcceptable() {
		for (State s : currentStates) {
			if (acceptableStates.contains(s)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public List<State> getActiveStates() {
		return this.currentStates;
	}

	@Override
	public void nextChar(String key) {
		if (!symbols.contains(key)) {
			throw new IllegalArgumentException("Key " + key + " is not a valid input for this automaton");
		}

		if (DEBUG) {
			System.out.println("Key: " + key);

			System.out.println("Current state list:");
			for (State q : currentStates)
				System.out.println("\t" + q);
		}

		/*
		 * For every state which is active in the current iteration, we need to
		 * determine the states which will follow based on the input symbol
		 */
		if (!currentStates.isEmpty()) {
			for (State q : currentStates) {

				/*
				 * For each applicable transition, add all the destination
				 * states to nextStates
				 */
				for (Transition t : q.transitions) {
					if (t.getKey().equals(key)) {
						for (State q2 : t.getDestinations()) {
							if (!nextStates.contains(q2))
								nextStates.add(q2);
						}
						if (DEBUG) {
							System.out.println("Aplicable transition: " + t);
						}
					}
				}

				/*
				 * For each state in nextStates, add all epsilon circles
				 */
				Vector<State> e = new Vector<State>();
				for (State qt : nextStates) {
					if (qt.hasEpsilonTransition())
						for (State qz : qt.eTransition.getDestinations())
							if (!nextStates.contains(qz))
								e.add(qz);
				}

				for (State q2 : e) {
					if (!nextStates.contains(q2)) {
						nextStates.add(q2);
					}
				}

			}

			/*
			 * If the Set nextStates is empty, we can safely clear currentStates
			 * and skip the next part
			 */

			if (nextStates.isEmpty()) {
				currentStates.clear();
				return;
			}

			/*
			 * Once we are done with the current iteration, we prepare the
			 * simulator for the next one by making nextStates currentStates
			 */
			currentStates.clear();
			currentStates.addAll(nextStates);
			nextStates.clear();

		} else {
			if (DEBUG) {
				System.out.println("\tSet is empty");
			}
		}
	}

	/**
	 * Initialises the automaton by clearing and rebuilding all state
	 * information
	 */
	private void initialise() {
		currentStates.clear();
		currentStates.add(startingState);
		if (startingState.hasEpsilonTransition()) {
			for (State q2 : startingState.eTransition.getDestinations()) {
				if (!currentStates.contains(q2))
					currentStates.add(q2);
			}
		}
		nextStates.clear();
	}

	private void minimise() {
		/*
		 * In order to make sure all epsilon transitions are properly simulated,
		 * we need to cache all possible destinations for each Transition
		 */

		boolean hasNextEpsilon;
		Vector<State> nstate = new Vector<State>();
		Vector<State> pstate = new Vector<State>();

		if (DEBUG) {
			System.out.println("===\nChecking epsilon circles\n===");
		}

		for (State q : epsilonInstances) {
			if (DEBUG) {
				System.out.println("epsilon-Circle for state " + q);
			}
			hasNextEpsilon = true;
			nstate.clear();
			pstate.clear();
			while (hasNextEpsilon) {
				hasNextEpsilon = false;
				for (State m : q.eTransition.getDestinations()) {
					/*
					 * We need to check if we already resolved the current
					 * state. If we did, we can avoid the work that follows.
					 */
					if (pstate.contains(m))
						continue;

					/*
					 * Add the current state to the circle and mark it as
					 * checked
					 */

					nstate.add(m);
					pstate.add(m);

					if (DEBUG) {
						System.out.println("\tChecking state " + m);
						System.out.println("\tState of nstate:");
						for (State qt : nstate) {
							System.out.println("\t\t" + qt);
						}
					}

					/*
					 * If the current state has more epsilon-transitions, we add
					 * them all into the current working set and set the
					 * appropriate flag
					 */

					if (m.hasEpsilonTransition()) {
						for (State q2 : m.eTransition.getDestinations()) {
							if (!nstate.contains(q2))
								nstate.add(q2);
						}
						hasNextEpsilon = true;
					}

				}

				for (State q1 : nstate) {
					if (!q.eTransition.getDestinations().contains(q1)) {
						if (DEBUG)
							System.out.println("\t>Adding " + q1);
						q.eTransition.getDestinations().add(q1);
					}
				}

				if (DEBUG) {
					System.out.println("epsilon-circle:");
					for (State q2 : q.eTransition.getDestinations()) {
						System.out.println("\t" + q2);
					}
				}

				nstate.clear();

				Collections.sort(q.eTransition.getDestinations());
			}
		}

	}

}
