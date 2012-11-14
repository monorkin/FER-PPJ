package hr.unizg.fer.zemris.ppj.maheri.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import hr.unizg.fer.zemris.ppj.maheri.parser.LrItem;
import hr.unizg.fer.zemris.ppj.maheri.parser.Production;
import hr.unizg.fer.zemris.ppj.maheri.symbol.NonTerminalSymbol;
import hr.unizg.fer.zemris.ppj.maheri.symbol.Symbol;
import hr.unizg.fer.zemris.ppj.maheri.symbol.TerminalSymbol;
import hr.unizg.fer.zemris.ppj.maheri.tests.TestUtils.TestData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class LrItemTest {

	private List<String> input;
	private List<String> output;

	public LrItemTest(final List<String> input, final List<String> output) {
		this.input = input;
		this.output = output;
	}

	@Parameters
	public static Collection<Object[]> data() {
		ArrayList<Object[]> data = new ArrayList<Object[]>();
		for (TestData t : TestUtils.loadData("LrItem")) {
			data.add(new Object[] { t.getInput(), t.getExpectedOutput() });
		}
		return data;
	}

	@Test
	public void test() {
		List<String> myOutput = new LinkedList<String>();
		for (String s : input) {
			String[] parts = s.split("->");
			NonTerminalSymbol key = new NonTerminalSymbol(parts[0]);
			List<List<Symbol>> productionDestination = new ArrayList<List<Symbol>>();
			if (parts.length > 1) {
				for (String s2 : parts[1].split("\\|")) {
					List<Symbol> symbols = new ArrayList<Symbol>();
					for (Character c : s2.toCharArray()) {
						if (Character.isUpperCase(c)) {
							symbols.add(new NonTerminalSymbol(String.valueOf(c)));
						} else {
							symbols.add(new TerminalSymbol(String.valueOf(c)));
						}
					}
					productionDestination.add(symbols);
				}
			} else {
				productionDestination.add(new ArrayList<Symbol>());
			}
			for (List<Symbol> e : productionDestination) {
				Production p = new Production(key, e);
				System.out.println(p);
				System.out.println(LrItem.fromProduction(p));
				for (LrItem item : LrItem.fromProduction(p)) {
					myOutput.add(item.toString());
				}
			}
		}
		assertEquals(output.size(), myOutput.size());
		for (String s : myOutput) {
			assertTrue(output.contains(s));
		}
	}

}
