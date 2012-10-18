package hr.unizg.fer.zemris.ppj.maheri.gen;

/**
 * This listener expects to be notified about the structure of a regex once it
 * is parsed and decomposed. The order of method calls with their arguments
 * gives a unique regex description.
 * 
 * This listener expects that it will be able to construct the expression (for
 * whichever purpose) "left to right" in one pass, assuming appropriate marks
 * and triggers are fired. </p>
 * 
 * <ul>
 * <li>{@link #onCharAppend(char)} signals a new character to the end</li>
 * 
 * <li>{@link #onBeforeBranch()} signals that the current position may be a
 * branching point, or should otherwise be designated as a separate branch</li>
 * 
 * <li>{@link #onNextBranch()} should signal completion of the branch (starting
 * from the last branching point and ending at the current position), and that
 * another branch between these two points follows one is to follow. If a
 * branching point is undefined, it is implicitly the first point in the expression</li>
 * 
 * <li>{@link #onAfterBranch()} means that all branches from the last branching
 * point have been completed. The branches merge at the current position. Error
 * if no branching point defined</li>
 * 
 * <li>{@link #onRepeat()} means that a section can be repeated or skipped. If
 * one or more branches join at the current point, all the branches are in the
 * repeating section, otherwise the section contains only the current position.</li>
 * 
 * </ul>
 * 
 * The example below outlines the contract the listener expects. If the
 * decomposer triggers events in this order, the regular expression
 * <code>cad|as(bf(a|c)ds|a)*xy(z*w)*</code> should be described
 * 
 * ( a b ) * branch, a, b,
 * 
 * <pre>
 * 
 *  append c
 * 	append a
 * 	append d
 * next
 * 	append a
 * 	append s
 * 	before
 * 		append b
 * 		append f
 * 		before
 * 			append a
 * 		next
 * 			append c
 * 		after
 * 		
 * 		append d
 * 		append s
 * 	next
 * 		append a
 * 	after
 * 	repeat
 * 	append x
 * 	append y
 * 	MARK
 * 	before
 * 		append z
 * 		repeat
 * 		append w
 * 	end nested
 * 	repeat last MARK
 * end nested
 * </pre>
 * 
 * @author dosvald
 */
public interface RegexCompositionListener {

	void onCharAppend(char c);

	void onBeforeBranch();

	void onNextBranch();

	void onAfterBranch();

	void onRepeat();

	// ... TODO incomplete
}
