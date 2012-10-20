package hr.unizg.fer.zemris.ppj.maheri.automaton;

import java.util.Vector;

public class Transition {
	public State origin;
	public Vector<State> destinations = new Vector<State>();
	public String key;

	@Override
	public String toString() {
		String c = "";
		if (destinations.isEmpty()) {
			c = "#";
		} else
			for (State q : destinations)
				c += q.name + ",";
		return this.origin + "," + this.key + "->"
				+ c.substring(0, c.length() - 1);
	}
}
