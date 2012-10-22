package hr.unizg.fer.zemris.ppj.maheri.lexergen;

import hr.unizg.fer.zemris.ppj.maheri.automaton.Automaton;
import hr.unizg.fer.zemris.ppj.maheri.automaton.State;
import hr.unizg.fer.zemris.ppj.maheri.automaton.Transition;
import hr.unizg.fer.zemris.ppj.maheri.automaton.eNfa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
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
			
			Map<String, List<String>> tmap = transitionsForState.get(from);
			if (tmap == null)
				tmap = new HashMap<String, List<String>>();
			
			transitionsForState.put(from, tmap);
			
			List<String> t = tmap.get(x);
			if (t == null)
				t = new LinkedList<String>();
				
			tmap.put(x, t);
			
			if (!t.contains(to))
				t.add(to);
			
		}

		private A() {
			stateNames.add(begin = createNewState());
			stateNames.add(end = createNewState());
			
		}
	}

	private static char unescape(char e) {
		switch (e) {
		case 't':
			return '\t';
		case 'n':
			return '\n';
		case '_':
			return ' ';
		default:
			return e;
		}
	}

	private static boolean isEscapedAt(String regex, int index) {
		index--;
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

		if (groups.size() > 1) {
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
				String s1;
				String s2;
				if (isEscapedAt(regex, i)) {
					s1 = createNewState();
					s2 = createNewState();
					curr.stateNames.add(s1);
					curr.stateNames.add(s2);
					curr.addTransition(s1, Character.toString(unescape(regex.charAt(i))), s2);
				} else {
					if (regex.charAt(i) == '\\')
						continue;

					if (regex.charAt(i) != '(') {
						s1 = createNewState();
						s2 = createNewState();
						curr.stateNames.add(s1);
						curr.stateNames.add(s2);
						curr.addTransition(s1, regex.charAt(i) == '$' ? "" : Character.toString(regex.charAt(i)), s2);
					} else {
						int j = findMatchingParenthesis(regex, i);
						A tmp = convert(regex.substring(i + 1, j));
						curr.stateNames.addAll(tmp.stateNames);
						curr.transitionsForState.putAll(tmp.transitionsForState);
						s1 = tmp.begin;
						s2 = tmp.end;
						i = j;
						
					}
				}

				if (i + 1 < regex.length() && regex.charAt(i + 1) == '*') {
					String innerBegin = s1;
					String innerEnd = s2;
					curr.stateNames.add(s1 = createNewState());
					curr.stateNames.add(s2 = createNewState());

					curr.addTransition(s1, "", innerBegin);
					curr.addTransition(innerEnd, "", s2);
					curr.addTransition(s1, "", s2);
					curr.addTransition(innerEnd, "", innerBegin);

					++i;
				}
				// /
				curr.addTransition(prev, "", s1);
				prev = s2;

			}
			curr.addTransition(prev, "", curr.end);
		}

		return curr;
	}
	
	public static eNfa getAutomaton(String regex) {
		A nfa = convert(regex);

		List<State> states = new LinkedList<>();
		List<String> symbols = new LinkedList<>();
		List<Transition> transitions = new LinkedList<>();
		// TODO determine the starting state and acceptable states
		List<State> acceptableStates = new LinkedList<>();

		// Generate state list
		for (String s : nfa.stateNames) {
			states.add(new State(s));
		}

		State startingState = State.getByName(nfa.begin, states);
		acceptableStates.add(State.getByName(nfa.end, states));

		/*
		 * Generate transition list
		 */
		for (String origin : nfa.transitionsForState.keySet()) {
			Map<String, List<String>> transitionMap = nfa.transitionsForState.get(origin);
			for (String key : transitionMap.keySet()) {
//				if (key.length() == 0)
//					key = Automaton.EPSILON;
				// DO MAGIC HERE ?
				List<State> destinations = new LinkedList<>();
				for (String s : transitionMap.get(key)) {
					destinations.add(State.getByName(s, states));
				}
				transitions.add(new Transition(State.getByName(origin, states), key, destinations));
			}
		}

		return new eNfa(states, null, transitions, startingState, acceptableStates);
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
