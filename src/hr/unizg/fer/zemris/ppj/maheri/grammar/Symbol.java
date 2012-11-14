package hr.unizg.fer.zemris.ppj.maheri.grammar;

import org.hamcrest.core.IsInstanceOf;

public class Symbol {
	private String name;
	private boolean isTerminal;

	public Symbol(String name, boolean isTerminal) {
		this.name = name;
		this.isTerminal = isTerminal;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	public boolean isTerminal() {
		return isTerminal;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Symbol))
			return false;

		Symbol tmp = (Symbol) obj;

		if (tmp.name.equals(this.name))
			return true;
		else
			return false;
	}
}