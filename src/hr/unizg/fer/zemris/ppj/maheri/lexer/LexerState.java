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
	
	
}
