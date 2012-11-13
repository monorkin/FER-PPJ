package hr.unizg.fer.zemris.ppj.maheri.parser;

import hr.unizg.fer.zemris.ppj.maheri.symbol.NonTerminalSymbol;
import hr.unizg.fer.zemris.ppj.maheri.symbol.Symbol;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * An extension of the LrItem class which can also contain a list of
 * nonterminating symbols
 * 
 * @author Petar Šegina <psegina@ymail.com>
 * 
 */
public class Lr1Item extends LrItem {

	protected Set<NonTerminalSymbol> nonterminalSymbols;

	public Lr1Item(NonTerminalSymbol symbol, List<List<Symbol>> production, int dotPosition) {
		super(symbol, production, dotPosition);
		Set<NonTerminalSymbol> t = new HashSet<NonTerminalSymbol>();
		t.add(new NonTerminalSymbol("#"));
		this.nonterminalSymbols = t;
	}

	public Lr1Item(LrItem item) {
		this(item.getKey(), item.getValue(), item.getDotPosition());
	}

	public Lr1Item(LrItem item, Set<NonTerminalSymbol> nonterminatingSymbols) {
		super(item.getKey(), item.getValue(), item.getDotPosition());
		this.nonterminalSymbols = nonterminatingSymbols;
	}

	@Override
	public String toString() {
		return String.format("%s %s", super.toString(), nonterminalSymbols.toString());
	}

}
