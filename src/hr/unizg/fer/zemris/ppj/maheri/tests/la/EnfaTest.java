package hr.unizg.fer.zemris.ppj.maheri.tests.la;

import static org.junit.Assert.assertEquals;
import hr.unizg.fer.zemris.ppj.maheri.automaton.State;
import hr.unizg.fer.zemris.ppj.maheri.automaton.Transition;
import hr.unizg.fer.zemris.ppj.maheri.automaton.eNfa;
import hr.unizg.fer.zemris.ppj.maheri.tests.TestUtils;
import hr.unizg.fer.zemris.ppj.maheri.tests.TestUtils.TestData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class EnfaTest {

	private List<String> input;
	private List<String> expectedOutput;

	public EnfaTest(List<String> input, List<String> output) {
		this.input = input;
		this.expectedOutput = output;
	}

	@Parameters
	public static Collection<Object[]> params() throws IOException {
		List<Object[]> data = new ArrayList<Object[]>();
		for (TestData t : TestUtils.loadData("enfa")) {
			data.add(new Object[] { t.getInput(), t.getExpectedOutput() });
		}
		return data;
	}

	@Test
	public void testAutomaton() {
		List<State> states = new LinkedList<State>();
		for (String s : input.get(1).split(",")) {
			states.add(new State(s));
		}
		List<String> symbols = Arrays.asList(input.get(2).split(","));
		List<State> acceptableStates = new LinkedList<State>();
		for (String s : input.get(3).split(",")) {
			acceptableStates.add(State.getByName(s, states));
		}
		State startingState = State.getByName(input.get(4), states);
		List<Transition> transitions = new LinkedList<Transition>();
		int i = 5;
		int len = input.size();
		for (i = 5; i < len; i++) {
			String transition = input.get(i);
			String[] parts = transition.split("->");
			List<State> destination = new LinkedList<State>();
			for (String s : parts[1].split(",")) {
				if (!s.equals("#")) {
					destination.add(State.getByName(s, states));
				}
			}
			String[] partsLeft = parts[0].split(",", -1);
			transitions.add(new Transition(State.getByName(partsLeft[0], states), partsLeft[1], destination));
		}

		eNfa automaton = new eNfa(states, symbols, transitions, startingState, acceptableStates);

		int iterationNumber = 0;
		for (String in : input.get(0).split("\\|")) {
			automaton.reset();
			StringBuilder output = new StringBuilder();
			for (State q : automaton.getActiveStates()) {
				output.append(q);
				output.append(",");
			}
			if (output.length() > 0) {
				output.delete(output.length() - 1, output.length());
				output.append("|");
			}
			for (String inputSymbol : in.split(",")) {
				automaton.nextChar(inputSymbol);
				if (automaton.getActiveStates().size() == 0) {
					output.append("#,");
				} else {
					for (State s : automaton.getActiveStates()) {
						output.append(s);
						output.append(",");
					}
				}
				if (output.length() > 0) {
					output.delete(output.length() - 1, output.length());
				}
				output.append("|");
			}
			output.delete(output.length() - 1, output.length());
			assertEquals(expectedOutput.get(iterationNumber), output.toString());
			iterationNumber++;
		}

	}

}
