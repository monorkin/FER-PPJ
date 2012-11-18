package hr.unizg.fer.zemris.ppj.maheri.tests.sa;

import static org.junit.Assert.*;

import hr.unizg.fer.zemris.ppj.maheri.automaton.DFA;
import hr.unizg.fer.zemris.ppj.maheri.automaton.DFAConvert;
import hr.unizg.fer.zemris.ppj.maheri.lexergen.RegexToAutomaton;
import hr.unizg.fer.zemris.ppj.maheri.tests.TestUtils;
import hr.unizg.fer.zemris.ppj.maheri.tests.TestUtils.TestData;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class RegexToAutomatonToDfaMatchingTest {

	private String regex;
	private String[] stringsToRunMatch;

	private DFA matcherAutomaton;

	/**
	 * @param regex
	 * @param stringsToRunMatch
	 */
	public RegexToAutomatonToDfaMatchingTest(String regex, String[] stringsToRunMatch) {
		this.regex = regex;
		this.stringsToRunMatch = stringsToRunMatch;

		matcherAutomaton = DFAConvert.fromENFA(RegexToAutomaton.getAutomaton(regex));
	}

	@Parameters
	public static Collection<Object[]> data() {
		List<Object[]> args = new LinkedList<Object[]>();

		args.add(new Object[] { "(a|b|c)*d", new String[] { "d", "ad", "cbaccad", "Doesntmatch", "" } });
		args.add(new Object[] { "$", new String[] { "", "ne", "$", "\\$", "foo$", "$foo" } });
		args.add(new Object[] { "\\$(0|1|2|3|4|5|6|7|8|9)*",
				new String[] { "", "ne", "$", "\\$", "foo$", "$foo", "$100", "$", "$$$" } });
		args.add(new Object[] { "(\\(|\\)|\\|)*x", new String[] { "|||x", "()()(x", "|(()(x", "x" } });

		for (TestData t : TestUtils.loadData("RegexToAutomatonMatching"))
			args.add(new Object[] { t.getInput().get(0), t.getExpectedOutput().toArray(new String[0]) });

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
			System.err.print("at start, state is: ");
			System.err.print(matcherAutomaton.getActiveState());
			System.err.println();

			for (int i = 0; i < s.length(); ++i) {
				matcherAutomaton.nextChar(Character.toString(s.charAt(i)));
				System.err.print("at char " + s.charAt(i) + ", state is: ");
				System.err.print(matcherAutomaton.getActiveState());
				System.err.println();
			}
			System.out.println(regex + "//" + s + " == " + s.matches(regex));
			assertEquals(regex + "//" + s, s.matches(regex), matcherAutomaton.isAcceptable());
		}
	}

}
