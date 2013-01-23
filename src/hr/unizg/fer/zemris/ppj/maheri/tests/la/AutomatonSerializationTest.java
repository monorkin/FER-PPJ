package hr.unizg.fer.zemris.ppj.maheri.tests.la;

import static org.junit.Assert.*;

import hr.unizg.fer.zemris.ppj.maheri.Logger;
import hr.unizg.fer.zemris.ppj.maheri.automaton.State;
import hr.unizg.fer.zemris.ppj.maheri.automaton.eNfa;
import hr.unizg.fer.zemris.ppj.maheri.lexergen.RegexToAutomaton;
import hr.unizg.fer.zemris.ppj.maheri.tests.TestUtils;
import hr.unizg.fer.zemris.ppj.maheri.tests.TestUtils.TestData;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class AutomatonSerializationTest {

	private String regex;
	private String[] stringsToRunMatch;

	private eNfa matcherAutomaton;

	/**
	 * @param regex
	 * @param stringsToRunMatch
	 */
	public AutomatonSerializationTest(String regex, String[] stringsToRunMatch) {
		this.regex = regex;
		this.stringsToRunMatch = stringsToRunMatch;

		matcherAutomaton = RegexToAutomaton.getAutomaton(regex);
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
		Logger.log("regex is " + regex);
		try {
			FileOutputStream stream = new FileOutputStream("res/testdata/AutomatonSerialization/automaton.ser");
			ObjectOutputStream oStream = new ObjectOutputStream(stream);
			oStream.writeObject(matcherAutomaton);
			Logger.log("Wrote Automaton");
			oStream.close();
			stream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		matcherAutomaton = null;
		
		try {
			FileInputStream file = new FileInputStream("res/testdata/AutomatonSerialization/automaton.ser");
			ObjectInputStream oin = new ObjectInputStream(file);
			matcherAutomaton =  (eNfa) oin.readObject();
			oin.close();
			file.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

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
			System.out.println(regex + "//" + s + " == " + s.matches(regex));
		}
	}

}
