package hr.unizg.fer.zemris.ppj.maheri.tests;

import static org.junit.Assert.*;
import hr.unizg.fer.zemris.ppj.maheri.lexergen.InputProcessor;
import hr.unizg.fer.zemris.ppj.maheri.lexergen.RegDefResolver;
import hr.unizg.fer.zemris.ppj.maheri.lexergen.structs.LexerRuleDescriptionText;
import hr.unizg.fer.zemris.ppj.maheri.tests.TestUtils.TestData;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;

public class InputProcessorTest {

	public InputProcessorTest() {

	}

	@Test
	public void test1() throws IOException {
		List<String> lines = Files.readAllLines(Paths.get("res/examples/lexergen-in/minusLang.lan"),
				Charset.defaultCharset());

		InputProcessor ip = new InputProcessor(lines);

		assertArrayEquals(
				new String[] {
						"{znamenka} 0|1|2|3|4|5|6|7|8|9",
						"{hexZnamenka} {znamenka}|a|b|c|d|e|f|A|B|C|D|E|F",
						"{broj} {znamenka}{znamenka}*|0x{hexZnamenka}{hexZnamenka}*",
						"{bjelina} \\t|\\n|\\_",
						"{sviZnakovi} \\(|\\)|\\{|\\}|\\||\\*|\\\\|\\$|\\t|\\n|\\_|!|\"|#|%|&|'|+|,|-|.|/|0|1|2|3|4|5|6|7|8|9|:|;|<|=|>|?|@|A|B|C|D|E|F|G|H|I|J|K|L|M|N|O|P|Q|R|S|T|U|V|W|X|Y|Z|[|]|^|_|`|a|b|c|d|e|f|g|h|i|j|k|l|m|n|o|p|q|r|s|t|u|v|w|x|y|z|~" },
				ip.getRegularDefinitions().toArray(new String[0]));

		assertArrayEquals(new String[] { "S_pocetno", "S_komentar", "S_unarni" },
				ip.getLexerStates().toArray(new String[0]));

		assertArrayEquals(new String[] { "OPERAND", "OP_MINUS", "UMINUS", "LIJEVA_ZAGRADA", "DESNA_ZAGRADA" }, ip.getTokenNames()
				.toArray(new String[0]));
		
		Map<String, String> regDef = new HashMap<String, String>();
		regDef.put("znamenka", "0|1|2|3|4|5|6|7|8|9");
		regDef.put("hexZnamenka", "(0|1|2|3|4|5|6|7|8|9)|a|b|c|d|e|f|A|B|C|D|E|F");
		regDef.put("broj", "(0|1|2|3|4|5|6|7|8|9)(0|1|2|3|4|5|6|7|8|9)*|0x((0|1|2|3|4|5|6|7|8|9)|a|b|c|d|e|f|A|B|C|D|E|F)((0|1|2|3|4|5|6|7|8|9)|a|b|c|d|e|f|A|B|C|D|E|F)*");
		regDef.put("bjelina", "\\t|\\n|\\_");
		regDef.put("sviZnakovi", "\\(|\\)|\\{|\\}|\\||\\*|\\\\|\\$|\\t|\\n|\\_|!|\"|#|%|&|'|+|,|-|.|/|0|1|2|3|4|5|6|7|8|9|:|;|<|=|>|?|@|A|B|C|D|E|F|G|H|I|J|K|L|M|N|O|P|Q|R|S|T|U|V|W|X|Y|Z|[|]|^|_|`|a|b|c|d|e|f|g|h|i|j|k|l|m|n|o|p|q|r|s|t|u|v|w|x|y|z|~");
		

	}
}
