package hr.unizg.fer.zemris.ppj.maheri.automaton;

import java.util.Vector;

public class State implements Comparable<State> {

		protected String name;
		protected Vector<Transition> transitions = new Vector<Transition>();;
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

		@Override
		public int compareTo(State arg0) {
			return this.name.compareTo(arg0.name);
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
