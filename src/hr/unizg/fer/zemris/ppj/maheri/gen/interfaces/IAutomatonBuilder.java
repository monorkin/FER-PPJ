package hr.unizg.fer.zemris.ppj.maheri.gen.interfaces;

import hr.unizg.fer.zemris.ppj.maheri.interfaces.IAutomaton;

/**
 * Interface using which automatons will be built for use in lexer. Automatons
 * being built shall have one initial and up to one accept state, the final
 * state. Construction is done internally using a simple process:
 * 
 * appending a character -> add transition to new state and make new state final
 * 
 * kleene operator -> add shortcircuit epsilon transition from begin to final
 * (for zero occurrences), and add epsilon transition from final state to
 * beginning (for repetition)
 * 
 * union operator -> create an epsilon transition to start states of all
 * operands
 * 
 * miscellaneous, terminology:
 * <ul>
 * <li> final state = the accept state if such exists </li>
 * <li> start = initial state </li>
 * <li> end = final state </li>
 * </ul>
 * 
 * @author dosvald
 */
public interface IAutomatonBuilder {

	/**
	 * Append a character.
	 * 
	 * The automaton under construction should accept the previously accepted
	 * regex with the argument character appended
	 * 
	 * @param c
	 *            character to append to regex this automaton recognizes
	 */
	public void append(char c);

	/**
	 * Zero or more ocurrences of the whole regex.
	 * 
	 * If the automaton under construction had previously accepted regex
	 * <code>R</code>, it shall now accept regex <code>R*</code>
	 */
	public void kleene();

	/**
	 * Concatenate one or more branches at end.
	 * 
	 * If the automaton under construction had previously accepted regex
	 * <code>R</code>, it shall now accept regex <code>R(S|...|...)</code>,
	 * where S, ... are the regexes accepted by the automatons given as
	 * arguments
	 * 
	 * @param arg automaton to append to the current
	 * @param alternatives optional extra automatons to append in union with {@code arg}
	 */
	public void concatenate(IAutomaton arg, IAutomaton... alternatives);
	
	/**
	 * Add one or more branches at start.
	 * 
	 * If the automaton under construction had previously accepted regex
	 * <code>R</code>, it shall now accept regex <code>(R|S|...)</code>,
	 * where S, ... are the regexes accepted by the automatons given as
	 * arguments
	 * 
	 * @param arg automaton to append to the current
	 * @param alternatives optional extra automatons to append in union with {@code arg}
	 * 
	 * @param alt
	 * @param alternatives
	 */
	public void union(IAutomaton alt, IAutomaton... alternatives);

	
	/**
	 * Build the automaton that was specified by calling appropriate methods.
	 * @return the automaton that has been constructed 
	 */
	IAutomaton getAutomaton();

}
