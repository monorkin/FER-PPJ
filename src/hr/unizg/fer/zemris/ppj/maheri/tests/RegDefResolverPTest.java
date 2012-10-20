package hr.unizg.fer.zemris.ppj.maheri.tests;

import hr.unizg.fer.zemris.ppj.maheri.lexergen.RegDefResolver;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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

		List<String> input1 = Files.readAllLines(Paths.get("res/testdata/RegexParserTest/1.in"), Charset.defaultCharset());
		List<String> output1 = Files.readAllLines(Paths.get("res/testdata/RegexParserTest/1.out"), Charset.defaultCharset());
		data.add(new Object[] { input1.toArray(new String[0]),
				output1.toArray(new String[0]) });
		return data;
	}

	@Test
	public void testOutputKeys() {
		Map<String, String> map = RegDefResolver.parseRegexes(input);
		for (String s : input) {
			assertTrue (map.keySet().contains(s.split(" ")[0]));
		}
	}

	@Test
	public void testOutputValues() {
		Map<String, String> map = RegDefResolver.parseRegexes(input);
		for (String s : output) {
			assertTrue (map.values().contains(s.split(" ")[1]));
		}
	}

}
