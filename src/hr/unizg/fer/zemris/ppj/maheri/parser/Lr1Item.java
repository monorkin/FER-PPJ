package hr.unizg.fer.zemris.ppj.maheri.parser;

import hr.unizg.fer.zemris.ppj.maheri.symbol.NonTerminalSymbol;
import hr.unizg.fer.zemris.ppj.maheri.symbol.Symbol;
import hr.unizg.fer.zemris.ppj.maheri.symbol.TerminalSymbol;

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

	protected Set<TerminalSymbol> terminalSymbols;

	public Lr1Item(NonTerminalSymbol symbol, List<Symbol> production, int dotPosition) {
		super(symbol, production, dotPosition);
		Set<TerminalSymbol> t = new HashSet<TerminalSymbol>();
		t.add(new TerminalSymbol("#"));
		this.terminalSymbols = t;
	}

	public Lr1Item(LrItem item) {
		this(item.getLeftHandSide(), item.getRightHandSide(), item.getDotPosition());
	}

	public Lr1Item(LrItem item, Set<TerminalSymbol> set) {
		super(item.getLeftHandSide(), item.getRightHandSide(), item.getDotPosition());
		this.terminalSymbols = set;
	}

	@Override
	public String toString() {
		return String.format("%s %s", super.toString(), terminalSymbols.toString());
	}

}
