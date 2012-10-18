package hr.unizg.fer.zemris.ppj.maheri.lexergen;

import hr.unizg.fer.zemris.ppj.maheri.lexergen.structs.LexerRuleDescriptionText;

import java.util.List;
import java.util.Map;

/**
 * This class lists what must be provided for the lexer generator to
 * successfully do shit
 * 
 * @author dosvald
 */
public class InputProcessor {

	/**
	 * @return list of regular definitions
	 */
	public List<String> getRegularDefinitions(){
		return null;}

	/**
	 * @return list of lexer states that will be used
	 */
	public List<String> getLexerStates(){
		return null;}

	/**
	 * @return list of names of tokens (lexemes) the lexer will tokenize the
	 *         input program into
	 */
	public List<String> getTokenNames(){
		return null;}

	/**
	 * @return list of {@link LexerRuleDescriptionText} objects, each describing
	 *         a match rule the lexer will follow when tokenizing input
	 * @param regDef
	 *            map containing regular definition names as keys, and resolved
	 *            regular expression as values. If this parameter is not
	 *            <code>null</code>, this method shall resolve regexes in rules
	 *            according to the definition map. Otherwise the rules are kept
	 *            verbatim
	 */
	public List<LexerRuleDescriptionText> getLexerRules(Map<String, String> regDef){
		return null;}

}