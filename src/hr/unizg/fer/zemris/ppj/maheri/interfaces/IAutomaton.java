package hr.unizg.fer.zemris.ppj.maheri.interfaces;

/**
 * 
 * 
 * miscellaneous, terminology
 * <ul>
 * <li>final state = the accept state if such exists</li>
 * <li>start = initial state</li>
 * <li>end = final state</li>
 * </ul>
 * 
 * @author dosvald
 */
public interface IAutomaton {

	/*
	 * TODO define interface so it makes sense with respect to IAutomatonBuilder
	 * (e.g. allow easy append at end or start), while at the same time being
	 * consistent for regex matching. Consider splitting into two - automaton
	 * under construction, and immutable automaton which is readonly and has
	 * regexmatching data, more specific to lexer and unrelated to regexes...
	 * 
	 * TODO Design classes for states, transitions and stuff the automaton needs
	 * to keep track of while matching
	 */

}
