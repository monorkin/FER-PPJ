package hr.unizg.fer.zemris.ppj.maheri.lexergen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegexToAutomaton {
	private static int stateNumber = 0;

	private static class A {
		private List<String> stateNames = new ArrayList<String>();
		private Map<String, Map<String, List<String>>> transitionsForState = new HashMap<String, Map<String, List<String>>>();
		private String begin;
		private String end;

		private void addTransition(String from, String x, String to) {
			List<String> t = new ArrayList<String>();
			Map<String, List<String>> u = new HashMap<String, List<String>>();
			t.add(to);
			u.put(x, t);
			transitionsForState.put(from, u);
		}

		private A() {
			begin = createNewState();
			end = createNewState();
		}
	}

	private static boolean isEscapedAt(String regex, int index) {
		boolean ret = false;
		while (index >= 0 && regex.charAt(index--) == '\\')
			ret = !ret;
		return ret;
	}

	private static int findMatchingParenthesis(String regex, int index) {
		int count = 0;
		for (int i = index + 1; i < regex.length(); ++i) {
			if (count == 0 && regex.charAt(i) == ')')
				return i;
			else if (regex.charAt(i) == '(')
				++count;
			else if (regex.charAt(i) == ')')
				--count;
		}
		return -1;
	}

	private static String createNewState() {
		return Integer.toString(stateNumber++);
	}

	private RegexToAutomaton(String regex) {
		boolean currCharEscaped = false;
		int len = regex.length();
		for (int i = 0; i < len; ++i) {
			if (regex.charAt(i) == '\\') {
				currCharEscaped = !currCharEscaped;
			}
		}
	}

	private static List<String> splitAndGroup(String regex) {

		List<Integer> splitPoints = new ArrayList<Integer>();

		int open = 0;
		int len = regex.length();
		for (int i = 0; i < len; ++i) {
			char c = regex.charAt(i);
			if (c == '(' && !isEscapedAt(regex, i)) {
				++open;
			} else if (c == ')' && !isEscapedAt(regex, i)) {
				--open;
			} else if (open == 0 && !isEscapedAt(regex, i) && c == '|') {
				splitPoints.add(i);
			}
		}

		List<String> ret = new ArrayList<String>();
		int prev = 0;
		for (int curr : splitPoints) {
			ret.add(regex.substring(prev, curr));
			prev = curr + 1;
		}
		if (prev < regex.length())
			ret.add(regex.substring(prev));

		return ret;
	}

	private static A convert(String regex) {
		List<String> groups = splitAndGroup(regex);
		A curr = new A();

		if (groups.size() > 0) {
			for (String s : groups) {
				A tmp = convert(s);
				curr.stateNames.addAll(tmp.stateNames);
				curr.transitionsForState.putAll(tmp.transitionsForState);
				curr.addTransition(curr.begin, "", tmp.begin);
				curr.addTransition(tmp.end, "", curr.end);
			}
		} else {
			String prev = curr.begin;
			for (int i = 0; i < regex.length(); ++i) {
				if (isEscapedAt(regex, i)) {
					if (regex.charAt(i) == 't')
				} else {
					
				}
				
				if (i+1 < regex.length() && i+1 == '*') {
					
				}
			}
		}

		return curr;
	}

	public static List<String> getAutomatonDescription(String regex) {
		A nfa = convert(regex);
		List<String> lines = new ArrayList<String>();
		StringBuilder first = new StringBuilder();

		for (int i = 0; i < nfa.stateNames.size(); ++i) {
			if (i > 0)
				first.append(',');
			first.append(nfa.stateNames.get(i));
		}

		lines.add(first.toString());

		for (String st : nfa.stateNames) {
			Map<String, List<String>> val = nfa.transitionsForState.get(st);
			if (val == null)
				continue;
			for (String in : val.keySet()) {
				List<String> val2 = val.get(in);
				if (val2 != null && val2.size() > 0) {
					StringBuilder next = new StringBuilder(st + ",");
					if (in.length() == 0)
						next.append('$');
					else
						next.append(in);
					next.append("->");
					for (int i = 0; i < val2.size(); ++i) {
						String targetState = val2.get(i);
						if (i > 0)
							next.append(',');
						next.append(targetState);
					}
					lines.add(next.toString());
				}
			}
		}
		return lines;
	}

}
