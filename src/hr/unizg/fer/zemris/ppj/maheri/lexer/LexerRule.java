package hr.unizg.fer.zemris.ppj.maheri.lexer;

import hr.unizg.fer.zemris.ppj.maheri.automaton.Automaton;

import java.io.Serializable;
import java.util.List;

public class LexerRule implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6224382889233758395L;

	private Automaton regex;
	private List<Action> actions;
	
	
	/**
	 * @param regex
	 * @param actions
	 */
	public LexerRule(Automaton regex, List<Action> actions) {
		this.regex = regex;
		this.actions = actions;
		
	}


	/**
	 * @return the regex
	 */
	public Automaton getRegex() {
		return regex;
	}


	/**
	 * @return the actions
	 */
	public List<Action> getActions() {
		return actions;
	}
	
	public void doActions(Lexer lexer) {
		for (Action a: actions) {
			a.doAction(lexer);
		}
	}
	
	
}
