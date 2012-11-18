package hr.unizg.fer.zemris.ppj.maheri.parser;

import hr.unizg.fer.zemris.ppj.maheri.Logger;
import hr.unizg.fer.zemris.ppj.maheri.symbol.NonTerminalSymbol;
import hr.unizg.fer.zemris.ppj.maheri.symbol.Symbol;
import hr.unizg.fer.zemris.ppj.maheri.symbol.TerminalSymbol;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Processes input and creates a formal grammar according to the specification
 * for the second laboratory
 * 
 * @author Petar Šegina <psegina@ymail.com>
 * 
 */
public class InputProcessor {
	
	/**
	 * Construct grammar from file in the format described in GLORIOUS PRIPREMA
	 * 
	 * @param lines
	 */
	public static Grammar parseInput(String[] lines) {
		List<TerminalSymbol> tsymbols = new ArrayList<TerminalSymbol>();
		List<NonTerminalSymbol> nsymbols = new ArrayList<NonTerminalSymbol>();
		List<Production> productions = new ArrayList<Production>();
		Set<TerminalSymbol> sync = new HashSet<TerminalSymbol>();

		String s = lines[0];

		s = s.replaceAll("%V ", "");
		String[] tmpsyms = s.split("\\s+");
		for (String ii : tmpsyms) {
			nsymbols.add(new NonTerminalSymbol(ii));
		}

		s = lines[1];

		s = s.replaceAll("%T ", "");
		tmpsyms = s.split("\\s+");
		for (String ii : tmpsyms) {
			tsymbols.add(new TerminalSymbol(ii));
		}
		
		s = lines[2];

		s = s.replaceAll("%Syn ", "");
		tmpsyms = s.split("\\s+");
		for (String ii : tmpsyms) {
			sync.add(new TerminalSymbol(ii));
		}

		ArrayList<Symbol> allSymbols = new ArrayList<Symbol>();
		allSymbols.addAll(nsymbols);
		allSymbols.addAll(tsymbols);

		NonTerminalSymbol currSymbol = null;
		for (int i = 3; i < lines.length; i++) {
			s = lines[i];
			if (!s.startsWith(" ")) {
				currSymbol = (NonTerminalSymbol) Symbol.getFromList(nsymbols, s);
			} else {
				productions.add(productionFromString(currSymbol, s.substring(1), allSymbols));
			}
		}
		
		Logger.log("Sync is " + sync);

		return new Grammar(productions, new HashSet<NonTerminalSymbol>(nsymbols),
				new HashSet<TerminalSymbol>(tsymbols), nsymbols.get(0), sync);
	}

	private static Production productionFromString(final NonTerminalSymbol leftSide, final String rightSideString,
			final ArrayList<Symbol> symbols) {
		List<Symbol> rightSide = new ArrayList<Symbol>();

		String[] rightSideArray = rightSideString.split("\\s+");

		for (String s : rightSideArray) {
			if (s.equals(Production.EPSILON))
				continue;
			rightSide.add(Symbol.getFromList(symbols, s));
		}
		return new Production(leftSide, rightSide);
	}
}
