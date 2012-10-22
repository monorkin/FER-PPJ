package hr.unizg.fer.zemris.ppj.maheri.lexer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class LexerState implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6021088691427970716L;
	
	private String stateName;

	public List<LexerRule> rules;

	/**
	 * @param rules
	 */
	public LexerState(List<LexerRule> rules) {
		this.rules = rules;
	}
	
	public String getName() {
		return stateName;
	}
	
	public LexerState(String stateName) {
		this.rules = new ArrayList<>();
		this.stateName = stateName;
	}

	/**
	 * @return the rules
	 */
	public List<LexerRule> getRules() {
		return rules;
	}
	
	public void addRule(LexerRule rule) {
		this.rules.add(rule);
	}

	public void pushCharToAutomatons(String c) {
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
			if (r.getRegex().isAcceptable()) return r;
		}
		return null;
	}
	
//	public void revertAutomatons() {
//		for (LexerRule r: rules) {
//			r.getRegex().undoOneStep();
//		}
//	}
	
	public void resetAutomatons() {
		for (LexerRule r: rules) {
			r.getRegex().reset();
		}
	}

}
