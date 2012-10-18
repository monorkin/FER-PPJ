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
}
