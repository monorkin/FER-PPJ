package hr.unizg.fer.zemris.ppj.maheri.parser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A representation of a formal grammar
 * 
 * @author Petar Šegina <psegina@ymail.com>
 * 
 */
public class Grammar {

	public static final String START_SYMBOL_FORMAT = "<_%s_>";

	private final Set<Production> productions;
	private final Set<String> nonterminalSymbols;
	private final Set<String> terminalSymbols;
	private final Map<String, Set<Production>> map;
	private String startSymbol;

	public Grammar(Set<Production> productions, Set<String> nonterminalSymbols, Set<String> terminalSymbols,
			String startSymbol) {
		this.productions = productions;
		this.terminalSymbols = terminalSymbols;
		this.nonterminalSymbols = nonterminalSymbols;
		this.startSymbol = startSymbol;
		map = new HashMap<String, Set<Production>>();
		for (Production p : productions) {
			if (map.get(p.getKey()) == null) {
				map.put(p.getKey(), new HashSet<Production>());
			}
			Set<Production> t = map.get(p.getKey());
			t.add(p);
			map.put(p.getKey(), t);
		}
	}

	public final Set<String> getNonterminalSymbols() {
		return nonterminalSymbols;
	}

	public Set<Production> getProductions() {
		return this.productions;
	}

	public Set<Production> getProductionsForSymbol(String nonterminatingSymbol) {
		return map.get(nonterminatingSymbol);
	}

	public final String getStartSymbol() {
		return startSymbol;
	}

	public final Set<String> getTerminalSymbols() {
		return terminalSymbols;
	}

	/**
	 * Modifies this grammar and introduces a new starting symbol with a
	 * production of <N>-><S> where <N> is the new starting symbol and <S> is
	 * the old starting symbol
	 */
	public final void createAlternateStartSymbol() {
		String newSymbol = String.format(START_SYMBOL_FORMAT, startSymbol.substring(1, startSymbol.length() - 1));
		nonterminalSymbols.add(newSymbol);
		productions.add(new Production(newSymbol, startSymbol));
		startSymbol = newSymbol;
	}

}
