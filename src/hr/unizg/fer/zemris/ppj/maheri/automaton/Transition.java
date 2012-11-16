package hr.unizg.fer.zemris.ppj.maheri.automaton;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class Transition implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6577490010100126978L;

	private final State origin;
	private final Set<State> destinations;
	private final String key;

	public Transition(final State origin, final String key, final List<State> destinations) {
		if (origin == null || key == null || destinations == null) {
			throw new IllegalArgumentException("Parameters must not be null!");
		}
		for (State q : destinations) {
			if (q == null) {
				throw new IllegalArgumentException("Null must not be present in destinations");
			}
		}
		this.origin = origin;
		this.key = key;
		this.destinations = new LinkedHashSet<State>(destinations);
	}

	public State getOrigin() {
		return origin;
	}

	public Set<State> getDestinations() {
		return destinations;
	}

	public String getKey() {
		return key;
	}

	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}

	@Override
	public boolean equals(Object that) {
		if (!(that instanceof Transition)) {
			return false;
		}
		Transition t = (Transition) that;

		for (State destination : destinations) {
			if (!t.getDestinations().contains(destination)) {
				return false;
			}
		}

		return this.key == t.getKey() && this.origin == t.origin;
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
