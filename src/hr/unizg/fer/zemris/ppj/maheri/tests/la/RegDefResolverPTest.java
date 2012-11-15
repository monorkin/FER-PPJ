package hr.unizg.fer.zemris.ppj.maheri.tests.la;

import static org.junit.Assert.assertTrue;
import hr.unizg.fer.zemris.ppj.maheri.lexergen.RegDefResolver;
import hr.unizg.fer.zemris.ppj.maheri.tests.TestUtils;
import hr.unizg.fer.zemris.ppj.maheri.tests.TestUtils.TestData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class RegDefResolverPTest {

	private String[] input;
	private String[] output;

	public RegDefResolverPTest(String[] input, String[] output) {
		this.input = input;
		this.output = output;
	}

	@Parameters
	public static Collection<Object[]> data() throws IOException {
		ArrayList<Object[]> data = new ArrayList<Object[]>();

		for (TestData t : TestUtils.loadData("RegexParserTest")) {
			data.add(new Object[] { t.getInput().toArray(new String[0]), t.getExpectedOutput().toArray(new String[0]) });
		}

		return data;
	}

	@Test
	public void testOutputKeys() {
		RegDefResolver r = new RegDefResolver(input);
		Map<String, String> map = r.getResolved();
		boolean matches;

		for (String s : input) {
			String key = s.split(" ")[0];
			key = key.substring(1, key.length()-1);
			matches = map.keySet().contains(key);
			if (!matches) {
				System.out.println("Key not found: " + key);
			}
			assertTrue(matches);
		}
	}

	@Test
	public void testOutputValues() {
		RegDefResolver r = new RegDefResolver(input);
		Map<String, String> map = r.getResolved();
		boolean matches;
		for (String s : output) {
			String output = s.split(" ")[1];
			matches = map.values().contains(output);
			if (!matches) {
				System.out.println("Value not found: " + output);
			}
			assertTrue(matches);
		}
	}

}
