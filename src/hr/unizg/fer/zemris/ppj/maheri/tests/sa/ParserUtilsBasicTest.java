package hr.unizg.fer.zemris.ppj.maheri.tests.sa;

import static org.junit.Assert.*;

import hr.unizg.fer.zemris.ppj.maheri.parser.Grammar;
import hr.unizg.fer.zemris.ppj.maheri.parser.InputProcessor;
import hr.unizg.fer.zemris.ppj.maheri.parser.ParserUtils;
import hr.unizg.fer.zemris.ppj.maheri.parser.Production;
import hr.unizg.fer.zemris.ppj.maheri.symbol.Symbol;
import hr.unizg.fer.zemris.ppj.maheri.symbol.TerminalSymbol;
import hr.unizg.fer.zemris.ppj.maheri.tests.TestUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class ParserUtilsBasicTest {

	private Grammar g;
	private List<Symbol> querySymbols;
	private TerminalSymbol[] expectedResults;

	private ParserUtils pu;

	public ParserUtilsBasicTest(Grammar g, String queryString, TerminalSymbol[] results) {
		this.g = g;

		querySymbols = new LinkedList<Symbol>();

		String[] strs = queryString.split(" ");
		for (String s : strs) {
			if (s.equals("$"))
				continue;
			Iterable<? extends Symbol> which;
			if (s.startsWith("<"))
				which = g.getNonterminalSymbols();
			else
				which = g.getTerminalSymbols();
			querySymbols.add(Symbol.getFromList(which, s));
		}

		expectedResults = results;

		pu = new ParserUtils(g);
	}

	@Test
	public final void testStartsWithSetListOfSymbol() {
		Set<TerminalSymbol> actual = pu.startsWithSet(querySymbols);
		assertEquals(expectedResults.length, actual.size());

		for (TerminalSymbol s : expectedResults)
			assertTrue(actual.contains(s));
	}

//	@Test
	public final void testStartsWithSetSymbol() {
	}

	@Parameters
	public static Collection<Object[]> data() throws IOException {
		List<Object[]> args = new LinkedList<Object[]>();
		
		List<String> lines = new LinkedList<String>();
		BufferedReader reader = new BufferedReader(new FileReader(new File(
				"res/testdata/SintaksniAnalizatorInputTest/gramatika100.in")));
		String line;
		while (null != (line = reader.readLine()))
			lines.add(line);
		reader.close();

		Grammar grammar = InputProcessor.parseInput(lines.toArray(new String[0]));
		
		args.add(new Object[] {grammar, "<E>", new TerminalSymbol[]{ new TerminalSymbol("e"), new TerminalSymbol("c") } });

		return args;
	}
}
