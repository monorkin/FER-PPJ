package hr.unizg.fer.zemris.ppj.maheri.symbol;

import java.util.List;

public abstract class Symbol {

	public static final String DOT = "*";
	public static final String ARROW = "->";
	public static final char BLANK = ' ';

	private final String value;
	private final boolean terminal;

	public Symbol(final String value, boolean terminal) {
		this.value = value;
		this.terminal = terminal;
	}

	public final String getValue() {
		return value;
	}

	@Override
	public boolean equals(Object that) {
		if (that instanceof String) {
			return this.value.equals(that);
		} else if (that instanceof Symbol) {
			Symbol t = (Symbol) that;
			return this.value.equals(t.value);
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return value.hashCode();
	}

	@Override
	public String toString() {
		return value;
	}

	public static Symbol getFromList(Iterable<? extends Symbol> haystack, String needle) {
		for (Symbol s : haystack) {
			if (s.equals(needle)) {
				return s;
			}
		}
		return null;
	}

	public boolean isTerminal() {
		return terminal;
	}
}
