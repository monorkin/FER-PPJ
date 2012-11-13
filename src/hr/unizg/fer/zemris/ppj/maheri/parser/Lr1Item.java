package hr.unizg.fer.zemris.ppj.maheri.parser;

import java.util.HashSet;
import java.util.Set;

/**
 * An extension of the LrItem class which can also contain a list of
 * nonterminating symbols
 * 
 * @author Petar Šegina <psegina@ymail.com>
 * 
 */
public class Lr1Item extends LrItem {

	protected Set<String> nonterminatingSymbols;

	public Lr1Item(String symbol, String production, int dotPosition) {
		super(symbol, production, dotPosition);
		Set<String> t = new HashSet<String>();
		t.add("#");
		this.nonterminatingSymbols = t;
	}

	public Lr1Item(LrItem item) {
		this(item.getKey(), item.getValue(), item.getDotPosition());
	}

	public Lr1Item(LrItem item, Set<String> nonterminatingSymbols) {
		super(item.getKey(), item.getValue(), item.getDotPosition());
		this.nonterminatingSymbols = nonterminatingSymbols;
	}

	@Override
	public String toString() {
		return String.format("%s %s", super.toString(), nonterminatingSymbols.toString());
	}

}
