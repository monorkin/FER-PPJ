package hr.unizg.fer.zemris.ppj.maheri.lexer;

import java.io.Serializable;

/**
 * Defines a single action that the lexer does (new line, change state, declare
 * as class...)
 * 
 * @author tljubej
 * 
 */
public interface Action extends Serializable {
	public void doAction(Lexer lexer);
}
