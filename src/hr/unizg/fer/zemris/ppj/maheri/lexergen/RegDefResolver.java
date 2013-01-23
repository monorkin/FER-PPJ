package hr.unizg.fer.zemris.ppj.maheri.lexergen;

import hr.unizg.fer.zemris.ppj.maheri.Logger;

import java.util.HashMap;
import java.util.Map;

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
						Logger.log("WARN: unescaped '{' in regdef reference");
					}
					break;
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
						Logger.log("WARN: invalid regdef value: stray '}', ignoring");
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
			Logger.log("WARN: invalid regdef: escape at end!");
		
		return value.toString();
	}

	public RegDefResolver(String[] array) {
		for (String line : array) {
			String[] splits = line.split(" ", 2);
			String name = splits[0].substring(1, splits[0].length() - 1);
			String rhs = splits[1];

			String value = resolve(rhs);
			
			map.put(name, value.toString());
		}
	}

	public Map<String, String> getResolved() {
		return map;
	}

}
