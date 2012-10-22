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

// test
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
	            	
	            	int pos = value.indexOf('>', 1);
	            	String activeState = value.substring(1, pos);
	            	String regex = value.substring(pos+1);
	            	
	            	RegDefResolver resolver = new RegDefResolver(RegularDefinitions.toArray(new String[0]));
	            	
	            	while (it.hasNext()) {
	            		value = it.next();
	            		if (value.charAt(0) == '{') {
	            			value = it.next();
	            			
	            			String action;
	            			List<String> extra = new ArrayList<String>();
	            			
	            			action = value;
	            			value = it.next();
	            			while(value.charAt(0) != '}') {
	            				extra.add(value);
	            				value = it.next();
	            			}
	            			
	            			System.err.printf("state = <%s>, regex = /%s/, action = [%s], ", activeState, regex, action);
	            			for (String s : extra)
	            				System.err.print(s + " ... ");
	            			System.err.println();
	            			
	            			LexerRules.add(new LexerRuleDescriptionText(activeState, resolver.resolve(regex), action, extra));
	            			
	            			break;
	            			
	            		}
	            	}
	            	Lexy += value;
	            }}
	        }
	 	
	}

// izrazi do kraja popodne, testovi do navecer, testiranja preko noci, rezultati sutra ujutro
/*
 Iterator<String> lr = lexerRuleString.iterator();
		 String activeStateNameIn = new String();
		 String regexStringIn = new String();
		 String actionNameIn = new String();
		 List<String> extraParameterLinesIn = new ArrayList<String>();
		 
		 while(lr.hasNext())
	        {
			 String val=(String)lr.next();
			 if(val.charAt(0) == '<'){
				 		String[] splitNameRegex = val.split(">");
				 		activeStateNameIn = splitNameRegex[0].substring(1, splitNameRegex[0].length());
				 		regexStringIn = splitNameRegex[1];
						val=(String)lr.next();
					}
			 if(val.charAt(0) == '{'){val=(String)lr.next();}
			 actionNameIn = val;
			 val=(String)lr.next();
			 while(val.charAt(0) != '}')
			 {
				 extraParameterLinesIn.add(val); 
				 val=(String)lr.next();
			 }					

			lexerRules.add(new LexerRuleDescriptionText(activeStateNameIn, regexStringIn, actionNameIn, extraParameterLinesIn));
			extraParameterLinesIn.clear();

			 
	        }
*/	
	
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
	 */
	public List<LexerRuleDescriptionText> getLexerRules() {
		return LexerRules;
	}

}