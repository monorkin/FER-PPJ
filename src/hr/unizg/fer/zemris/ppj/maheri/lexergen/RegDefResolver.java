package hr.unizg.fer.zemris.ppj.maheri.lexergen;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * can parse the input and return a Key-Value map with the regexes resolved
 * 
 * @author Aleksandar "Nisam-jos-gotov-s-12-linija-u-4-dana" Gavrilovic
 * 
 */
public class RegDefResolver {

	/**
	 * Transforms the input into a key-value map
	 * 
	 * @param input
	 *           The input to parse and resolve regexes
	 * @return A key-value map
	 */
	public static Map<String, String> parseRegexes(String[] array) {
		 Map<String,String> m = new HashMap<String, String>();
        String name="";
        String value="";
        String tempValue="";
        // napravimo mapu iz stringova
        for(String i: array) {
            value="";
            Scanner scanner = new Scanner(i);
            scanner.useDelimiter("[{}]");
            name = scanner.next().trim();
            while (scanner.hasNext())
            {
                tempValue += scanner.next().trim();
                if (m.get(tempValue)!=null) 
                {
                    value+="("+m.get(tempValue)+")";

                }
                else value+=tempValue;
                tempValue="";
            }
            m.put(name, value);
            System.err.println("Name is:" + name + ", and Value is:" + value);
            }
        
        return m;
	}

}
