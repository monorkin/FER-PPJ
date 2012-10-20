package hr.unizg.fer.zemris.ppj.maheri.automaton;

import java.util.List;

public class Transition {
	private final State origin;
	private final List<State> destinations;
	private final String key;

	public Transition(final State origin, final String key, final List<State> destinations) {
		if (origin == null || key == null || destinations == null) {
			throw new IllegalArgumentException("Parameters must not be null!");
		}
		this.origin = origin;
		this.key = key;
		this.destinations = destinations;
	}

	public State getOrigin() {
		return origin;
	}

	public List<State> getDestinations() {
		return destinations;
	}

	public String getKey() {
		return key;
	}

	@Override
	public String toString() {
		String c = "";
		if (destinations.isEmpty()) {
			c = "#";
		} else
			for (State q : destinations)
				c += q.name + ",";
		return this.origin + "," + this.key + "->" + c.substring(0, c.length() - 1);
	}
}
