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
            String rightSide = i.substring(1+name.length()+1+1); //jer je {nekaj}_
            int escapeCounter=0;
            String rightSide2="";
            for (int j=0; j<rightSide.length(); j++) {
                String c=rightSide.substring(j,j+1);
                if (c.equals("\\")) escapeCounter++;
                else if (c.equals("{") && escapeCounter%2==1) {
                    c="ł";
                    escapeCounter=0;
                }
                else if (c.equals("}") && escapeCounter%2==1) {
                    c="Ł";
                    escapeCounter=0;
                }
                else escapeCounter=0;
                rightSide2=rightSide2+c;
            }
            Scanner scanner2 = new Scanner(rightSide2);
            scanner2.useDelimiter("[{}]");
            while (scanner2.hasNext())
            {
                tempValue += scanner2.next().trim();
                if (m.get(tempValue)!=null) 
                {
                    value+="("+m.get(tempValue)+")";
                }
                else 
                    value+=tempValue;
                tempValue="";
            }
            String value2="";
            for (int j=0; j<value.length(); j++) {
                String c=value.substring(j,j+1);
                if (c.equals("ł")) c="{";
                else if (c.equals("Ł")) c="}";
                value2=value2+c;
            }
            m.put(name, value2);
            System.out.println(m);
            }
        return m;
	}

}
