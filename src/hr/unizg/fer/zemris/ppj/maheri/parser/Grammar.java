package hr.unizg.fer.zemris.ppj.maheri.parser;

import hr.unizg.fer.zemris.ppj.maheri.symbol.NonTerminalSymbol;
import hr.unizg.fer.zemris.ppj.maheri.symbol.Symbol;
import hr.unizg.fer.zemris.ppj.maheri.symbol.TerminalSymbol;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
	private final Set<NonTerminalSymbol> nonterminalSymbols;
	private final Set<TerminalSymbol> terminalSymbols;
	private final Map<NonTerminalSymbol, Set<Production>> map;
	private NonTerminalSymbol startSymbol;

	public Grammar(Set<Production> productions, Set<NonTerminalSymbol> nonterminalSymbols,
			Set<TerminalSymbol> terminalSymbols, NonTerminalSymbol startSymbol) {
		this.productions = productions;
		this.terminalSymbols = terminalSymbols;
		this.nonterminalSymbols = nonterminalSymbols;
		this.startSymbol = startSymbol;
		map = new HashMap<NonTerminalSymbol, Set<Production>>();
		for (Production p : productions) {
			if (map.get(p.getKey()) == null) {
				map.put(p.getKey(), new HashSet<Production>());
			}
			Set<Production> t = map.get(p.getKey());
			t.add(p);
			map.put(p.getKey(), t);
		}
	}

	public final Set<NonTerminalSymbol> getNonterminalSymbols() {
		return nonterminalSymbols;
	}

	public Set<Production> getProductions() {
		return this.productions;
	}

	public Set<Production> getProductionsForSymbol(NonTerminalSymbol nonterminatingSymbol) {
		return map.get(nonterminatingSymbol);
	}

	public final NonTerminalSymbol getStartSymbol() {
		return startSymbol;
	}

	public final Set<TerminalSymbol> getTerminalSymbols() {
		return terminalSymbols;
	}

	/**
	 * Modifies this grammar and introduces a new starting symbol with a
	 * production of <N>-><S> where <N> is the new starting symbol and <S> is
	 * the old starting symbol
	 */
	public final void createAlternateStartSymbol() {
		NonTerminalSymbol newSymbol = new NonTerminalSymbol(String.format(START_SYMBOL_FORMAT, startSymbol));
		nonterminalSymbols.add(newSymbol);
		List<List<Symbol>> productionDestination = new ArrayList<List<Symbol>>(1);
		productionDestination.add(Arrays.asList(new Symbol[] { newSymbol }));
		productions.add(new Production(newSymbol, productionDestination));
		startSymbol = newSymbol;
	}

}
