package hr.unizg.fer.zemris.ppj.maheri.tests;

import static org.junit.Assert.assertEquals;
import hr.unizg.fer.zemris.ppj.maheri.parser.InputProcessor;
import hr.unizg.fer.zemris.ppj.maheri.tests.TestUtils.TestData;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Tests the 2nd input processor, as well as serializing a Grammar to string
 * 
 * @author Petar Šegina <psegina@ymail.com>
 * 
 */
@RunWith(Parameterized.class)
public class SAInputProcessorTest {

	List<String> input;
	List<String> expectedOutput;

	public SAInputProcessorTest(List<String> input, List<String> expectedOutput) {
		this.input = input;
		this.expectedOutput = expectedOutput;
	}

	@Parameters
	public static Collection<Object[]> params() {
		List<Object[]> result = new LinkedList<Object[]>();
		for (TestData t : TestUtils.loadData("SintaksniAnalizatorInputTest")) {
			result.add(new Object[] { t.getInput(), t.getExpectedOutput() });
		}
		return result;
	}

	@Test
	public void test() {
		String[] output = InputProcessor.parseInput(input).toString().split("\n");
		for (int i = 0; i < expectedOutput.size(); ++i) {
			assertEquals(expectedOutput.get(i), output[i]);
		}
	}

}
