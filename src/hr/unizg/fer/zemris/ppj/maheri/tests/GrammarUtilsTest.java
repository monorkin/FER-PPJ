package hr.unizg.fer.zemris.ppj.maheri.tests;

import hr.unizg.fer.zemris.ppj.maheri.parser.Grammar;
import hr.unizg.fer.zemris.ppj.maheri.parser.ParserUtils;
import hr.unizg.fer.zemris.ppj.maheri.parser.InputProcessor;
import hr.unizg.fer.zemris.ppj.maheri.symbol.NonTerminalSymbol;
import hr.unizg.fer.zemris.ppj.maheri.symbol.TerminalSymbol;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class GrammarUtilsTest {
	
	static Set<NonTerminalSymbol> nonTerminal = new HashSet<NonTerminalSymbol>();
	static Set<TerminalSymbol> terminal = new HashSet<TerminalSymbol>();
	
	
	public static void main(String[] args) {
		FileInputStream fajl=null;
		try {
			fajl=new FileInputStream("res/testdata/lab2parser/1.in");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Scanner scan = new Scanner(fajl);
		
		List<String> llines = new ArrayList<String>();
		
		while (scan.hasNextLine()) {
			llines.add(scan.nextLine());
		}
		
		
		String[] lines= llines.toArray(new String[0]);
		
		Grammar grmr = InputProcessor.parseInput(lines);
		
		System.out.println(grmr);
		
		ParserUtils util = new ParserUtils(grmr);
		
		System.out.println(util);
	}


}
