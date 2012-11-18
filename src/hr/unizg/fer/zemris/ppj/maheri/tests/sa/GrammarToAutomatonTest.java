package hr.unizg.fer.zemris.ppj.maheri.tests.sa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import hr.unizg.fer.zemris.ppj.maheri.Logger;
import hr.unizg.fer.zemris.ppj.maheri.automaton.Automaton;
import hr.unizg.fer.zemris.ppj.maheri.automaton.State;
import hr.unizg.fer.zemris.ppj.maheri.automaton.Transition;
import hr.unizg.fer.zemris.ppj.maheri.parser.Grammar;
import hr.unizg.fer.zemris.ppj.maheri.parser.InputProcessor;
import hr.unizg.fer.zemris.ppj.maheri.parser.ParserUtils;
import hr.unizg.fer.zemris.ppj.maheri.symbol.Symbol;
import hr.unizg.fer.zemris.ppj.maheri.tests.TestUtils;
import hr.unizg.fer.zemris.ppj.maheri.tests.TestUtils.TestData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class GrammarToAutomatonTest {

	private final List<String> expectedOutput;
	private final Grammar grammar;
	private final ParserUtils parserUtils;
	private final Automaton automaton;

	public GrammarToAutomatonTest(List<String> input, List<String> expectedOutput) {
		this.expectedOutput = expectedOutput;
		this.grammar = InputProcessor.parseInput(input.toArray(new String[0]));
		this.parserUtils = new ParserUtils(grammar);
		this.automaton = this.parserUtils.automatonFromGrammar();
	}

	@Parameters
	public static Collection<Object[]> data() {
		List<Object[]> data = new ArrayList<Object[]>();
		for (TestData t : TestUtils.loadData("GrammarToAutomaton")) {
			data.add(new Object[] { t.getInput(), t.getExpectedOutput() });
		}
		return data;
	}

	@Test
	public void automatonHasCorrectSymbolList() {
		Set<Symbol> allSymbols = new HashSet<Symbol>(grammar.getNonterminalSymbols());
		allSymbols.addAll(grammar.getTerminalSymbols());
		List<Symbol> syms = new ArrayList<Symbol>(allSymbols);
		Collections.sort(syms);
		List<String> automatonSymbols = automaton.getSymbols();
		Collections.sort(automatonSymbols);

		assertEquals(allSymbols.size(), automatonSymbols.size());

		int i = 0;
		for (Symbol s : syms) {
			assertEquals(s, automatonSymbols.get(i++));
		}

	}

	@Test
	public void automatonAllStatesAreAcceptable() {
		assertTrue(automaton.getAcceptableStates().equals(automaton.getStates()));
	}

	@Test
	public void startingStateIsNeverOnTheRightSide() {
		State start = automaton.getStartState();
		for (Transition t : automaton.getTransitions()) {
			assertFalse(t.getDestinations().contains(start));
		}
	}

	@Test
	public void testTransitionList() {
		int i = 0;
		outer:
		for (Transition t : automaton.getTransitions()) {
			Logger.log("Searching for "+t);
			for(String output : expectedOutput) {
				if(output.equals(toString(t))) {
					Logger.log("Found: "+toString(t));
					continue outer;
				} else {
//					Logger.log("");
//					Logger.log(output);
//					Logger.log(toString(t));
				}
			}
			Logger.log("Not found: "+toString(t));
			assertFalse(true);
		}
	}

	private static String toString(Transition t) {
		return String.format("%s >>> %s >>> %s", t.getOrigin(), t.getKey() == "" ? "$" : t.getKey(),
				t.getDestinations().iterator().next());
	}

}
