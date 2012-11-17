package hr.unizg.fer.zemris.ppj.maheri.automaton;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;
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
	
	public Transition(final State origin, final String key, final State singleDestination) {
		this(origin, key, Arrays.asList(new State[]{singleDestination}));
	}

	public State getOrigin() {
		return origin;
	}

	public Set<State> getDestinations() {
		return destinations;
	}
	
	public State getDestination() {
		Iterator<State> it = destinations.iterator();
		State ret = it.next();
		if (it.hasNext())
			throw new IllegalStateException("Have multiple destinations");
		return ret;
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

		if (!t.destinations.equals(destinations))
			return false;

		return this.key.equals(t.getKey()) && this.origin == t.origin;
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
