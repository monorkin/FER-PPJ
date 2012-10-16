package hr.unizg.fer.zemris.ppj.maheri.gen;

/**
 * This listener listens expects to be notified about the structure of a regex
 * once it is parsed and decomposed. The order of method calls with their
 * arguments gives a unique regex description.
 * 
 * This listener expects that in order to build the regex, TODO...
 * 
 * @author dosvald
 */
public interface RegexCompositionListener {

	void onCharAppend(char c);
	
	void onUnionStart();
	
	void onUnionOption();
	
	void onUnionEnd();
	
	// ... TODO incomplete
}
