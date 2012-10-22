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

	Map<String, String> map = new HashMap<String, String>();
	
	public String resolve(String regexWithRefs) {
		StringBuilder value = new StringBuilder();
		
		boolean escaped = false;
		int braceIndex = -1;
		for (int i = 0; i < regexWithRefs.length(); ++i) {
			char curr = regexWithRefs.charAt(i);
			if (braceIndex >= 0) {
				switch (curr) {
				case '\\':
					escaped = !escaped;
					break;
				case '}':
					if (escaped) {
						escaped = false;
					} else {
						value.append("(").append(map.get(regexWithRefs.substring(braceIndex + 1, i))).append(")");
						braceIndex = -1;
					}
					break;
				case '{':
					if (escaped) {
						escaped = false;
					} else {
						System.err.println("WARN: unescaped '{' in regdef reference");
					}
				default:
					if (escaped) {
						escaped = false;
					}
					continue;
				}
			} else {
				switch (curr) {
				case '\\':
					if (escaped) {
						value.append('\\').append('\\');
					}
					escaped = !escaped;
					break;
				case '{':
					if (escaped) {
						// ???
						value.append('{');
						escaped = false;
					} else {
						braceIndex = i;
					}
					break;
				case '}':
					if (escaped) {
						// ???
						value.append('}');
						escaped = false;
					} else {
						System.err.println("WARN: invalid regdef value: stray '}', ignoring");
					}
					break;
				default:
					if (escaped) {
						value.append('\\').append(curr);
						escaped = false;
					} else {
						value.append(curr);
					}
				}
			}
		}
		if (escaped)
			System.err.println("WARN: invalid regdef: escape at end!");
		
		return value.toString();
	}

	public RegDefResolver(String[] array) {
		for (String line : array) {
			String[] splits = line.split(" ", 2);
			String name = splits[0].substring(1, splits[0].length() - 1);
			String rhs = splits[1];

			String value = resolve(rhs);
			
			map.put(name, value.toString());
			System.out.println(line + " ### [" + name + "], " + value);
		}
	}

	public Map<String, String> getResolved() {
		return map;
	}

	/**
	 * Transforms the input into a key-value map
	 * 
	 * @param input
	 *            The input to parse and resolve regexes
	 * @return A key-value map
	 */
	public static Map<String, String> pars2eRegexes(String[] array) {
		Map<String, String> m = new HashMap<String, String>();
		String name = "";
		String value = "";
		String tempValue = "";
		// napravimo mapu iz stringova
		for (String i : array) {
			value = "";
			Scanner scanner = new Scanner(i);
			scanner.useDelimiter("[{}]");
			name = scanner.next().trim();
			String rightSide = i.substring(1 + name.length() + 1 + 1); // jer je
																		// {nekaj}_
			int escapeCounter = 0;
			String rightSide2 = "";
			for (int j = 0; j < rightSide.length(); j++) {
				String c = rightSide.substring(j, j + 1);
				if (c.equals("\\"))
					escapeCounter++;
				else if (c.equals("{") && escapeCounter % 2 == 1) {
					c = "ł";
					escapeCounter = 0;
				} else if (c.equals("}") && escapeCounter % 2 == 1) {
					c = "Ł";
					escapeCounter = 0;
				} else
					escapeCounter = 0;
				rightSide2 = rightSide2 + c;
			}
			Scanner scanner2 = new Scanner(rightSide2);
			scanner2.useDelimiter("[{}]");
			while (scanner2.hasNext()) {
				tempValue += scanner2.next().trim();
				if (m.get(tempValue) != null) {
					value += "(" + m.get(tempValue) + ")";
				} else
					value += tempValue;
				tempValue = "";
			}
			String value2 = "";
			for (int j = 0; j < value.length(); j++) {
				String c = value.substring(j, j + 1);
				if (c.equals("ł"))
					c = "{";
				else if (c.equals("Ł"))
					c = "}";
				value2 = value2 + c;
			}
			m.put(name, value2);
//			System.out.println(m);
		}
		return new RegDefResolver(array).getResolved();
//		return m;
	}

}
