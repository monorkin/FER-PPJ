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

	private final Set<TerminalSymbol> terminalSymbols;

	public Lr1Item(LrItem item, Set<TerminalSymbol> set) {
		super(item.getLeftHandSide(), item.getRightHandSide(), item.getDotPosition());
		this.terminalSymbols = set;
	}

	public Set<TerminalSymbol> getTerminalSymbols() {
		return terminalSymbols;
	}

	@Override
	public boolean equals(Object that) {
		if (!(that instanceof Lr1Item)) {
			return false;
		}
		Lr1Item other = (Lr1Item) that;
		return this.getDotPosition() == other.getDotPosition()
				&& this.getLeftHandSide().equals(other.getLeftHandSide())
				&& this.getTerminalSymbols().equals(other.getTerminalSymbols())
				&& this.getRightHandSide().equals(other.getRightHandSide());
	}

	@Override
	public String toString() {
		StringBuilder syms = new StringBuilder();
		if (terminalSymbols.isEmpty()) {
			syms.append(Symbol.END);
			syms.append(" ");
		} else {
			for (Symbol s : terminalSymbols) {
				syms.append(s.toString());
				syms.append(" ");
			}
		}
		return String.format("%s {%s}", super.toString(), syms);
	}
	
}
