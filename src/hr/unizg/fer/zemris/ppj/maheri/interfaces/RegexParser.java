package hr.unizg.fer.zemris.ppj.maheri.interfaces;

import java.util.Map;

/**
 * Representation of a module which will parse the input and return a Key-Value
 * map with the regexes resolved
 * 
 * @author Petar Segina <psegina@ymail.com>
 * 
 */
public interface RegexParser {

	/**
	 * Transforms the input into a key-value map
	 * 
	 * @param input
	 *           The input to parse and resolve regexes
	 * @return A key-value map
	 */
	public Map<String, String> parseRegexes(String[] input);

}
