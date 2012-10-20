package hr.unizg.fer.zemris.ppj.maheri.lexergen;

import hr.unizg.fer.zemris.ppj.maheri.lexergen.structs.LexerRuleDescriptionText;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class lists what must be provided for the lexer generator to
 * successfully do shit
 * 
 * @author dosvald, crocoder
 */
public class InputProcessor {
	
	List<String> RegularDefinitions = new ArrayList<String>();
	List<String> LexerStates = new ArrayList<String>();
	List<String> TokenNames = new ArrayList<String>();
	String Lexy = new String();
	List<LexerRuleDescriptionText> LexerRules = new ArrayList<LexerRuleDescriptionText>();
	
	
	public InputProcessor(List<String> inputLines) {
		Integer mode = 1;
		//throw new RuntimeException("unimplemented");

		 Iterator<String> it= inputLines.iterator();

	     while(it.hasNext())
	        {
	         String value=(String)it.next();   
	         if(value.charAt(0) == '%'){
	        	  if(value.charAt(1) == 'X')
	        		  mode = 2;
	        	  else if(value.charAt(1) == 'L')
	        		  mode = 3;
	          }

	         if((mode == 3) && (value.charAt(0) == '<'))
	         {
	        	 		mode = 4;
	         }		
	          switch (mode) {
	            case 1:  
	            {
	            		 RegularDefinitions.add(value); break;
	            }
	            case 2: 
	            {	
	            	    value = value.substring(3, value.length());
	            		for(String a: value.split(" "))
	            		{
	            		  LexerStates.add(a); 
	            		}
	            		break;
	            }
	            case 3:  
	            {
            	        value = value.substring(3, value.length());
            		    for(String b: value.split(" "))
            		    {
            			 TokenNames.add(b); 
            		    }
            		    break;
	            }
	            case 4:
	            {
	            	Lexy += value;
	            }}
	        }
	 	
	}

	
	
	/**
	 * @return list of regular definitions
	 */
	public List<String> getRegularDefinitions() {
		return RegularDefinitions;
	}

	/**
	 * @return list of lexer states that will be used
	 */
	public List<String> getLexerStates() {
		return LexerStates;
	}

	/**
	 * @return list of names of tokens (lexemes) the lexer will tokenize the
	 *         input program into
	 */
	public List<String> getTokenNames() {
		return TokenNames;
	}

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
	public List<LexerRuleDescriptionText> getLexerRules(Map<String, String> regDef) {
		/* TODO: LEXY JE STRING KOJI SADRZI STATE NAME, REGULARNI IZRAZ I AKCIJU. 
		*  POSAVJETOVATI SE SA GOSP. OSVALD ASAP
		*/
		return LexerRules;
	}

}