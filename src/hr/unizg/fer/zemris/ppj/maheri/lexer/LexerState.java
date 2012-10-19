package hr.unizg.fer.zemris.ppj.maheri.lexer;

import java.io.Serializable;
import java.util.List;

public class LexerState implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6021088691427970716L;

	public List<LexerRule> rules;

	/**
	 * @param rules
	 */
	public LexerState(List<LexerRule> rules) {
		this.rules = rules;
	}

	/**
	 * @return the rules
	 */
	public List<LexerRule> getRules() {
		return rules;
	}

	public void pushCharToAutomatons(char c) {
		for (LexerRule r : rules) {
			r.getRegex().nextChar(c);
		}
	}

	public boolean isAnyAlive() {
		for (LexerRule r : rules) {
			if (r.getRegex().isAlive())
				return true;
		}
		return false;
	}
	
	public LexerRule getAccepted() {
		for (LexerRule r: rules) {
			if (r.getRegex().doesAccept()) return r;
		}
		return null;
	}

}
