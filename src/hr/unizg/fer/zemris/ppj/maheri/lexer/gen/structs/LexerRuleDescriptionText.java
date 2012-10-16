package hr.unizg.fer.zemris.ppj.maheri.lexer.gen.structs;

import java.util.List;

/**
 * Simple wrapper for info in lexer rule (only string data) Lexer follows rules
 * if it is in its rule's desired state. The rule which matches longest string
 * in input is acted upon. Action can have extra parameters.
 * 
 * @author dosvald
 */
public class LexerRuleDescriptionText {
	private String activeStateName;
	private String regexString;
	private String actionName;
	private List<String> extraParameterLines;
	
	

	/**
	 * Constructor from all fields
	 * @param activeStateName
	 * @param regexString
	 * @param actionName
	 * @param extraParameterLines
	 */
	public LexerRuleDescriptionText(String activeStateName, String regexString, String actionName,
			List<String> extraParameterLines) {
		this.activeStateName = activeStateName;
		this.regexString = regexString;
		this.actionName = actionName;
		this.extraParameterLines = extraParameterLines;
	}

	/**
	 * @return the name of the state the lexer must be in to follow this rule
	 */
	public String getActiveStateName() {
		return activeStateName;
	}

	/**
	 * @return the original regex the lexer is trying to match for this rule
	 */
	public String getRegexString() {
		return regexString;
	}

	/**
	 * @return the name of the action in the rule
	 */
	public String getActionName() {
		return actionName;
	}

	/**
	 * @return extra parameter lines provided for this rule action
	 */
	public List<String> getExtraParameterLines() {
		return extraParameterLines;
	}
}