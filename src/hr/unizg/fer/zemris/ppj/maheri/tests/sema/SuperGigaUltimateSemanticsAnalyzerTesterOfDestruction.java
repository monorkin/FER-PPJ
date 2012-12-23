package hr.unizg.fer.zemris.ppj.maheri.tests.sema;

import hr.unizg.fer.zemris.ppj.maheri.semantics.InputProcessor;
import hr.unizg.fer.zemris.ppj.maheri.semantics.Node;
import hr.unizg.fer.zemris.ppj.maheri.semantics.SemanticsAnalyzer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class SuperGigaUltimateSemanticsAnalyzerTesterOfDestruction {
	private String fileName;

	public SuperGigaUltimateSemanticsAnalyzerTesterOfDestruction(String fileName) {
		this.fileName = fileName;
	}

	@Parameters
	public static List<String[]> getFileNames() {
		File files = new File("res/examples/seman-in-extra/");
		File[] listFiles = files.listFiles();
		
		Arrays.sort(listFiles);
		List<String[]> fileNames = new ArrayList<String[]>();
		for (File f : listFiles) {
			String fName = f.getName();
			String[] arej = new String[1];
			arej[0] = fName.substring(0, fName.length() - 3);
			fileNames.add(arej);
		}
		return fileNames;
	}

	@Test
	public void superDuperTest() throws Exception {
		System.out.println("\nTESTISUJEMO TEST PRIMJER: " + fileName);
		String output = null;
		try {
			output = getAnalyzerOutputFromFile("res/examples/seman-in-extra/" + fileName + ".in");
		} catch (Exception e) {
			System.out.println("------------");
			System.out.println("NEŠTO OŠLO U KURAC");
			e.printStackTrace();
			System.out.println("------------");
			throw e;

		}
		String correctOutput = null;
		try {
			Scanner scn = new Scanner(new File("res/examples/seman-out-extra/" + fileName + ".out"));
			scn.useDelimiter("\\Z");
			correctOutput =scn.next();
			scn.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		System.out.println("------------");
		System.out.println("DOBIVENO: " + output);
		System.out.println("OČEKIVANO: " + correctOutput);
		System.out.println("------------");
		assertEquals(correctOutput, output);
	}

	public String getAnalyzerOutputFromFile(String filename) throws IOException {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(filename));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		List<String> inputLines = new ArrayList<String>();

		String currentLine;
		while ((currentLine = reader.readLine()) != null) {
			inputLines.add(currentLine);
		}
		reader.close();
		InputProcessor ip = new InputProcessor(inputLines);
		Node tree = ip.getTree();

		return new SemanticsAnalyzer(tree).check().getOutput();

	}

	// @Test
	// public void simpleTest() throws IOException {
	// String
	// output=getAnalyzerOutputFromFile("res/examples/seman-in/01_idn.in");
	// String correctOutput=new Scanner(new
	// File("res/examples/seman-out/01_idn.out")).useDelimiter("\\Z").next();
	//
	// assertEquals(correctOutput, output);
	//
	//
	// }
}