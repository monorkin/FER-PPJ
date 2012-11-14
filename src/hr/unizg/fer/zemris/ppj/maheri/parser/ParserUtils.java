package hr.unizg.fer.zemris.ppj.maheri.parser;

import hr.unizg.fer.zemris.ppj.maheri.automaton.Automaton;
import hr.unizg.fer.zemris.ppj.maheri.symbol.NonTerminalSymbol;
import hr.unizg.fer.zemris.ppj.maheri.symbol.Symbol;
import hr.unizg.fer.zemris.ppj.maheri.symbol.TerminalSymbol;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class ParserUtils {

	private Grammar grammar;

	private Set<Symbol> emptySymbols;
	private char[][] startsWithSymbol;

	private List<Production> productions;
	private ArrayList<Symbol> symbols;

	/**
	 * @param grammar
	 */
	ParserUtils(Grammar grammar) {
		this.grammar = grammar;

		productions = grammar.getProductions();
		symbols = new ArrayList<Symbol>(grammar.getNonterminalSymbols());
		symbols.addAll(grammar.getTerminalSymbols());

		generateSets();
	}

	private void generateSets() {
		startsWithSymbol = new char[symbols.size()][symbols.size()];
		for (int i = 0; i < symbols.size(); i++) {
			for (int j = 0; j < symbols.size(); j++) {
				startsWithSymbol[i][j] = '0';
			}
		}

		emptySymbols = new HashSet<Symbol>();

		// Empty symbols
		for (Production p : productions) {
			if (p.getRightHandSide().isEmpty()) {
				emptySymbols.add(p.getLeftHandSide());
			}
		}

		boolean change = true;
		while (change) {
			change = false;
			for (Production p : productions) {
				boolean allEmpty = true;
				for (Symbol s : p.getRightHandSide()) {
					if (!emptySymbols.contains(s))
						allEmpty = false;
				}
				if (allEmpty) {
					change = true;
					emptySymbols.add(p.getLeftHandSide());
				}
			}
		}
		// End empty symbols

		// Starts directly with
		for (Production p : productions) {
			for (Symbol s : p.getRightHandSide()) {
				startsWithSymbol[symbols.indexOf(p.getLeftHandSide())][symbols.indexOf(s)] = '1';
				if (!emptySymbols.contains(s))
					break;
			}
		}
		// End starts directly with

		// Starts with
		for (int i = 0; i < symbols.size(); i++) {
			for (int j = 0; j < symbols.size(); j++) {
				if (i == j) {
					startsWithSymbol[i][j] = '*';
					continue;
				}
				if (startsWithSymbol[i][j] == '1') {
					for (int k = 0; k < symbols.size(); k++) {
						if (startsWithSymbol[j][k] == '1')
							startsWithSymbol[i][k] = '*';
					}
				}
			}
		}
		// End starts with
	}

	/**
	 * Gets "starts with" set for arbitrary string of symbols
	 * 
	 * @param symString
	 * @return
	 */
	public Set<TerminalSymbol> startsWithSet(List<Symbol> symString) {
		Set<TerminalSymbol> symSet = new HashSet<TerminalSymbol>();

		for (Symbol s : symString) {
			symSet.addAll(startsWithSet(s));
			if (!emptySymbols.contains(s))
				break;
		}

		return symSet;
	}

	/**
	 * 
	 * @param symString
	 * @return true if string of symbols contains only empty symbols
	 */
	public boolean isStringEmpty(List<Symbol> symString) {
		for (Symbol s : symString) {
			if (!emptySymbols.contains(s))
				return false;
		}
		return true;
	}

	/**
	 * Gets "starts with" set for a single symbol
	 * 
	 * @param sym
	 * @return
	 */
	public Set<TerminalSymbol> startsWithSet(Symbol sym) {
		int symIndex = symbols.indexOf(sym);
		Set<TerminalSymbol> symSet = new HashSet<TerminalSymbol>();

		for (int i = 0; i < symbols.size(); i++) {
			if (startsWithSymbol[symIndex][i] == '1' || startsWithSymbol[symIndex][i] == '*') {
				if (symbols.get(i).isTerminal())
					symSet.add((TerminalSymbol) symbols.get(i));
			}
		}

		return symSet;
	}

	public static final String TRANSITION_FORMAT = "%s,%s";

	public Automaton automatonFromGrammar() {
		grammar.createAlternateStartSymbol();
		Set<Lr1Item> lrItems = new HashSet<Lr1Item>();
		for (Production p : grammar.getProductions()) {
			for (LrItem item : LrItem.fromProduction(p)) {
				lrItems.add(new Lr1Item(item, startsWithSet(item.getRightHandSide())));
			}
		}

		// TODO psegina : generate transition map
		// TODO psegina : transform to DFA

		throw new UnsupportedOperationException("Not yet implemented");
	}
}
