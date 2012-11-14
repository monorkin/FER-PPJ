package hr.unizg.fer.zemris.ppj.maheri.parser;

import hr.unizg.fer.zemris.ppj.maheri.symbol.NonTerminalSymbol;
import hr.unizg.fer.zemris.ppj.maheri.symbol.Symbol;
import hr.unizg.fer.zemris.ppj.maheri.symbol.TerminalSymbol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * Processes input and creates a formal grammar according to the specification
 * for the second laboratory
 * 
 * @author Petar Šegina <psegina@ymail.com>
 * 
 */
public class InputProcessor {

	public static Grammar parseInput(List<String> input) {
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
			productions.add(new Production(sym, productionMap.get(sym)));
		}

		return new Grammar(productions, new HashSet<NonTerminalSymbol>(nonTerminalSymbols),
				new HashSet<TerminalSymbol>(terminalSymbols), nonTerminalSymbols.get(0));
	}
}
