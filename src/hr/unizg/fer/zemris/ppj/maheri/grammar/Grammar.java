package hr.unizg.fer.zemris.ppj.maheri.grammar;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Grammar {
	private List<Symbol> symbols;
	private List<Production> productions;
	private Set<Symbol> emptySymbols;
	private char[][] startsWithSymbol;

	/**
	 * @param productions
	 */
	public Grammar(List<Production> productions) {
		this.productions = productions;
	}

	/**
	 * Construct grammar from file in the format described in GLORIOUS PRIPREMA
	 * 
	 * @param lines
	 */
	public Grammar(String[] lines) {
		symbols = new ArrayList<Symbol>();
		productions = new ArrayList<Production>();

		String s = lines[0];

		s.replaceAll("%V ", "");
		String[] tmpsyms = s.split("\\s+");
		for (String ii : tmpsyms) {
			symbols.add(new Symbol(ii, false));
		}

		s = lines[1];

		s.replaceAll("%T ", "");
		tmpsyms = s.split("\\s+");
		for (String ii : tmpsyms) {
			symbols.add(new Symbol(ii, true));
		}

		Symbol currSymbol = null;
		for (int i = 3; i < lines.length; i++) {
			s = lines[i];
			if (!s.startsWith(" ")) {
				currSymbol = getSymbol(s);
			} else {
				productions.add(new Production(currSymbol, s.substring(1), symbols));
			}
		}

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
			if (p.getRightSide().isEmpty()) {
				emptySymbols.add(p.getLeftSide());
			}
		}

		boolean change = true;
		while (change) {
			change = false;
			for (Production p : productions) {
				boolean allEmpty = true;
				for (Symbol s : p.getRightSide()) {
					if (!emptySymbols.contains(s))
						allEmpty = false;
				}
				if (allEmpty) {
					change = true;
					emptySymbols.add(p.getLeftSide());
				}
			}
		}
		// End empty symbols

		// Starts directly with
		for (Production p : productions) {
			for (Symbol s : p.getRightSide()) {
				startsWithSymbol[symbols.indexOf(p.getLeftSide())][symbols.indexOf(s)] = '1';
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
	 * @param symString
	 * @return
	 */
	public Set<Symbol> startsWithSet(List<Symbol> symString) {
		Set<Symbol> symSet = new HashSet<Symbol>();

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
	public Set<Symbol> startsWithSet(Symbol sym) {
		int symIndex = symbols.indexOf(sym);
		Set<Symbol> symSet = new HashSet<Symbol>();

		for (int i = 0; i < symbols.size(); i++) {
			if (startsWithSymbol[symIndex][i] == '1' || startsWithSymbol[symIndex][i] == '*') {
				if (symbols.get(i).isTerminal())
					symSet.add(symbols.get(i));
			}
		}

		return symSet;
	}

	/**
	 * Gets symbol by name
	 * 
	 * @param sym
	 * @return
	 */
	private Symbol getSymbol(String sym) {
		for (Symbol s : symbols) {
			if (s.getName().equals(sym))
				return s;
		}
		return null;
	}

	// private Symbol getEmptySymbol(String sym) {
	// for (Symbol s : emptySymbols) {
	// if (s.getName().equals(sym))
	// return s;
	// }
	// return null;
	// }

}