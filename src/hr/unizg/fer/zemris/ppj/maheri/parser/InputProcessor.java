package hr.unizg.fer.zemris.ppj.maheri.parser;

import hr.unizg.fer.zemris.ppj.maheri.symbol.NonTerminalSymbol;
import hr.unizg.fer.zemris.ppj.maheri.symbol.Symbol;
import hr.unizg.fer.zemris.ppj.maheri.symbol.TerminalSymbol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * Processes input and creates a formal grammar according to the specification
 * for the second laboratory
 * 
 * @author Petar Šegina <psegina@ymail.com>
 * 
 */
public class InputProcessor {

	public static Grammar parseInput2(List<String> input) {
		List<NonTerminalSymbol> nonTerminalSymbols = new ArrayList<NonTerminalSymbol>();
		String nonterminal = input.get(0);
		nonterminal = nonterminal.substring(nonterminal.indexOf(" ")).trim();
		for (String s : nonterminal.split(" ")) {
			nonTerminalSymbols.add(new NonTerminalSymbol(s));
		}

		List<TerminalSymbol> terminalSymbols = new ArrayList<TerminalSymbol>();
		String terminal = input.get(1);
		terminal = terminal.substring(terminal.indexOf(" ")).trim();
		for (String s : terminal.split(" ")) {
			terminalSymbols.add(new TerminalSymbol(s));
		}

		NonTerminalSymbol currentSource = null;
		List<Production> productions = new ArrayList<Production>();
		List<NonTerminalSymbol> ntOrder = new ArrayList<NonTerminalSymbol>();
		HashMap<NonTerminalSymbol, List<List<Symbol>>> productionMap = new HashMap<NonTerminalSymbol, List<List<Symbol>>>();

		for (NonTerminalSymbol s : nonTerminalSymbols) {
			productionMap.put(s, new ArrayList<List<Symbol>>());
		}

		for (int i = 3; i < input.size(); ++i) {
			String line = input.get(i);
			if (line.charAt(0) == Symbol.BLANK) {
				line = line.trim();
				List<Symbol> destination = new ArrayList<Symbol>();
				for (String s : line.split(" ")) {
					Symbol sym = Symbol.getFromList(nonTerminalSymbols, s);
					if (sym == null) {
						sym = Symbol.getFromList(terminalSymbols, s);
					}
					destination.add(sym);
				}
				productionMap.get(currentSource).add(destination);
			} else {
				currentSource = (NonTerminalSymbol) Symbol.getFromList(nonTerminalSymbols, line.trim());
				if (!ntOrder.contains(currentSource)) {
					ntOrder.add(currentSource);
				}
			}
		}

		for (NonTerminalSymbol sym : ntOrder) {
//			productions.add(new Production(sym, productionMap.get(sym)));
		}

		return new Grammar(productions, new HashSet<NonTerminalSymbol>(nonTerminalSymbols),
				new HashSet<TerminalSymbol>(terminalSymbols), nonTerminalSymbols.get(0));
	}

	/**
	 * Construct grammar from file in the format described in GLORIOUS PRIPREMA
	 * 
	 * @param lines
	 */
	public static Grammar parseInput(String[] lines) {
		List<TerminalSymbol> tsymbols = new ArrayList<TerminalSymbol>();
		List<NonTerminalSymbol> nsymbols = new ArrayList<NonTerminalSymbol>();
		List<Production> productions = new ArrayList<Production>();

		String s = lines[0];

		s.replaceAll("%V ", "");
		String[] tmpsyms = s.split("\\s+");
		for (String ii : tmpsyms) {
			nsymbols.add(new NonTerminalSymbol(ii));
		}

		s = lines[1];

		s.replaceAll("%T ", "");
		tmpsyms = s.split("\\s+");
		for (String ii : tmpsyms) {
			tsymbols.add(new TerminalSymbol(ii));
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

		return new Grammar(productions, new HashSet<NonTerminalSymbol>(nsymbols),
				new HashSet<TerminalSymbol>(tsymbols), nsymbols.get(0));
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
