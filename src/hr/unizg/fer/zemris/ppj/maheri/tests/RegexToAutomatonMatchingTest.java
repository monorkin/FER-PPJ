package hr.unizg.fer.zemris.ppj.maheri.tests;

import static org.junit.Assert.*;

import hr.unizg.fer.zemris.ppj.maheri.automaton.State;
import hr.unizg.fer.zemris.ppj.maheri.automaton.eNfa;
import hr.unizg.fer.zemris.ppj.maheri.lexergen.RegexToAutomaton;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class RegexToAutomatonMatchingTest {

	private String regex;
	private String[] stringsToRunMatch;

	private eNfa matcherAutomaton;

	/**
	 * @param regex
	 * @param stringsToRunMatch
	 */
	public RegexToAutomatonMatchingTest(String regex, String[] stringsToRunMatch) {
		this.regex = regex;
		this.stringsToRunMatch = stringsToRunMatch;

		matcherAutomaton = RegexToAutomaton.getAutomaton(regex);
	}

	@Parameters
	public static Collection<Object[]> data() {
		List<Object[]> args = new LinkedList<Object[]>();

		args.add(new Object[] { "(a|b|c)*d", new String[] { "d", "ad", "cbaccad", "Doesntmatch", "" } });
		args.add(new Object[] { "(a|b|c)*d", new String[] { "d", "ad", "cbaccad", "Doesntmatch", "$#@REFS" } });
		//
		// args.add(new Object[]{});
		// args.add(new Object[]{});
		// args.add(new Object[]{});

		return args;
	}

	@Test
	public void test() {
		for (String s : stringsToRunMatch) {
			matcherAutomaton.reset();
			System.err.print("at start, states are: ");
			for (State state : matcherAutomaton.getActiveStates())
				System.err.print(state.toString() + ", ");
			System.err.println();
			
			for (int i = 0; i < s.length(); ++i) {
				matcherAutomaton.nextChar(Character.toString(s.charAt(i)));
				System.err.print("at char " + s.charAt(i) + ", states are: ");
				for (State state : matcherAutomaton.getActiveStates())
					System.err.print(state.toString() + ", ");
				System.err.println();
			}
			assertEquals(regex + "//" + s, s.matches(regex), matcherAutomaton.isAcceptable());
		}
	}

}
