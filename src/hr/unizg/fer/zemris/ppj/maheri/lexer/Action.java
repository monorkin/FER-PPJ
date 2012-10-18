package hr.unizg.fer.zemris.ppj.maheri.lexer;

/**
 * Defines a single action that the lexer does (new line, change state, declare
 * as class...)
 * 
 * @author tljubej
 * 
 */
public interface Action {
	public void doAction(Lexer lexer);
}
