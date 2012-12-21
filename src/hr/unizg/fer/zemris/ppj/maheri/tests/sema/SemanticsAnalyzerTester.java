package hr.unizg.fer.zemris.ppj.maheri.tests.sema;

import hr.unizg.fer.zemris.ppj.maheri.semantics.InputProcessor;
import hr.unizg.fer.zemris.ppj.maheri.semantics.Node;
import hr.unizg.fer.zemris.ppj.maheri.semantics.SemanticsAnalyzer;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.junit.Test;

public class SemanticsAnalyzerTester {
	@Test
	public void simpleTest() throws IOException {
		//morat ću nać neki pametniji način
//		List<String> inputLines = new ArrayList<String>();
//
//		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(
//				"res/examples/seman-in/01_idn.in")));
//		String currentLine;
//		while ((currentLine = reader.readLine()) != null) {
//			inputLines.add(currentLine);
//		}
//
//		InputProcessor ip = new InputProcessor(inputLines);
//		Node tree = ip.getTree();
//		
//		PipedInputStream pin = new PipedInputStream();  
//	    PipedOutputStream pout = new PipedOutputStream(pin);  
//	   
//	    PrintStream out = new PrintStream(pout); 
//	    
//	    SemanticsAnalyzer an = new SemanticsAnalyzer(tree, out);
//	    an.checkAttributes();
//	    an.checkFunctions();
//	    
//	    BufferedReader in = new BufferedReader(new InputStreamReader(pin));
//	    
//	    Scanner scan = 
	}
}
