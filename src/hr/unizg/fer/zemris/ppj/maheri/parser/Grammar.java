package hr.unizg.fer.zemris.ppj.maheri.parser;

import hr.unizg.fer.zemris.ppj.maheri.symbol.NonTerminalSymbol;
import hr.unizg.fer.zemris.ppj.maheri.symbol.Symbol;
import hr.unizg.fer.zemris.ppj.maheri.symbol.TerminalSymbol;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
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

	private final List<Production> productions;
	private final Set<NonTerminalSymbol> nonterminalSymbols;
	private final Set<TerminalSymbol> terminalSymbols;
	private final Map<NonTerminalSymbol, Set<Production>> map;
	private NonTerminalSymbol startSymbol;

	public Grammar(List<Production> productions, Set<NonTerminalSymbol> nonterminalSymbols,
			Set<TerminalSymbol> terminalSymbols, NonTerminalSymbol startSymbol) {
		this.productions = productions;
		this.terminalSymbols = terminalSymbols;
		this.nonterminalSymbols = nonterminalSymbols;
		this.startSymbol = startSymbol;
		map = new HashMap<NonTerminalSymbol, Set<Production>>();
		for (Production p : productions) {
			if (map.get(p.getLeftHandSide()) == null) {
				map.put(p.getLeftHandSide(), new LinkedHashSet<Production>());
			}
			Set<Production> t = map.get(p.getLeftHandSide());
			t.add(p);
			map.put(p.getLeftHandSide(), t);
		}
	}

	public final Set<NonTerminalSymbol> getNonterminalSymbols() {
		return nonterminalSymbols;
	}

	public List<Production> getProductions() {
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
		productions.add(new Production(newSymbol, Arrays.asList(new Symbol[] { startSymbol })));
		startSymbol = newSymbol;
	} 

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Production p : getProductions()) {
			sb.append(p.toString());
			sb.append("\n");
		}
		return sb.toString();
	}
}
