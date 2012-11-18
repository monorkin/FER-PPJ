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


//	/**
//	 * @param args
//	 */
//	public static void main(String[] args) {
//
//		nonTerminal.add(new NonTerminalSymbol("<A>"));
//		nonTerminal.add(new NonTerminalSymbol("<B>"));
//		nonTerminal.add(new NonTerminalSymbol("<C>"));
//		nonTerminal.add(new NonTerminalSymbol("<D>"));
//		nonTerminal.add(new NonTerminalSymbol("<E>"));
//		
//		terminal.add(new TerminalSymbol("a"));
//		terminal.add(new TerminalSymbol("b"));
//		terminal.add(new TerminalSymbol("c"));
//		terminal.add(new TerminalSymbol("d"));
//		terminal.add(new TerminalSymbol("e"));
//		terminal.add(new TerminalSymbol("f"));
//		
//		List<Production> prods = new ArrayList<Production>();
//		
//		List<Symbol> syms1 = new ArrayList<Symbol>();
//		syms1.add(getNT("<B>"));
//		syms1.add(getNT("<C>"));
//		syms1.add(getT("c"));
//		prods.add(new Production(getNT("<A>"), syms1));
//		
//		List<Symbol> syms2 = new ArrayList<Symbol>();
//		syms1.add(getT("e"));
//		syms1.add(getNT("<D>"));
//		syms1.add(getNT("<B>"));
//		prods.add(new Production(getNT("<A>"), syms2));
//		
//		prods.add(new Production(getNT("<A>"), new ArrayList<Symbol>()));
//
//	}
//	
//	private static TerminalSymbol getT(String str) {
//		return new TerminalSymbol(str);
//	}
//	
//	private static NonTerminalSymbol getNT(String str) {
//		return new NonTerminalSymbol(str);
//	}

}
