package hr.unizg.fer.zemris.ppj.maheri.tests;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Utilities which should allow for easier building of unit tests
 * 
 * @author Petar Šegina <psegina@ymail.com>
 * 
 */
public class TestUtils {

	public static class TestData {
		private final List<String> input;
		private final List<String> expectedOutput;
		private String name;

		public TestData(final String name, final List<String> input, final List<String> expectedOutput) {
			this.input = input;
			this.expectedOutput = expectedOutput;
			this.name = name;
		}

		public TestData(final List<String> input, final List<String> expectedOutput) {
			this("Unknown", input, expectedOutput);
		}

		public List<String> getInput() {
			return this.input;
		}

		public List<String> getExpectedOutput() {
			return this.expectedOutput;
		}

		public String getName() {
			return this.name;
		}

	}

	/**
	 * Loads test data for the requested component
	 * 
	 * @param componentName
	 *            The component for which the data will be loaded, typically
	 *            from res/testdata/{component_name}
	 * @return A list of TestData objects, where each one contains the input and
	 *         the expected output
	 */
	public static List<TestData> loadData(final String componentName) {
		List<TestData> result = new ArrayList<TestData>();
		File componentDir = new File("res/testdata/" + componentName);
		if (!componentDir.exists() || !componentDir.isDirectory()) {
			throw new IllegalArgumentException("Test directory does not exist or is invalid");
		}
		List<File> testCandidates = traversePath(componentDir);
		for (File in : testCandidates) {
			if (in.getName().endsWith(".in")) {
				File out = new File(in.getPath().replace(".in", ".out"));
				try {
					List<String> linesIn = new LinkedList<String>();
					BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(
							in.getPath()))));
					String currentLine;
					while ((currentLine = reader.readLine()) != null) {
						linesIn.add(currentLine);
					}
					reader.close();

					List<String> linesOut = new LinkedList<String>();
					reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(out.getPath()))));
					while ((currentLine = reader.readLine()) != null) {
						linesOut.add(currentLine);
					}
					reader.close();
					TestData t;
					t = new TestData(linesIn, linesOut);
					result.add(t);
				} catch (IOException e) {
					System.err.println("error loading test data for " + componentName);
					e.printStackTrace();
				}
			}
		}

		return result;
	}

	public static List<File> traversePath(File root) {
		List<File> files = new LinkedList<File>();
		for (String file : root.list()) {
			File f = new File(root, file);
			if (f.isDirectory()) {
				files.addAll(traversePath(f));
			} else {
				files.add(f);
			}
		}
		return files;
	}

}
