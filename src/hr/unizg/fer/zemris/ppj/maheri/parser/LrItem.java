package hr.unizg.fer.zemris.ppj.maheri.parser;

import hr.unizg.fer.zemris.ppj.maheri.symbol.NonTerminalSymbol;
import hr.unizg.fer.zemris.ppj.maheri.symbol.Symbol;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LrItem extends Production {

	private final int dotPosition;

	public LrItem(final NonTerminalSymbol symbol, final List<Symbol> production, final int dotPosition) {
		super(symbol, production);
		if (dotPosition > production.size() || dotPosition < 0) {
			throw new IllegalArgumentException("Dot position is out of range: " + dotPosition);
		}
		this.dotPosition = dotPosition;
	}

	/**
	 * Creates new LrItems from a production
	 * 
	 * @key production the production from which to generate the items
	 * 
	 */
	public static Set<LrItem> fromProduction(Production production) {
		Set<LrItem> result = new HashSet<LrItem>();

		int len = production.getRightHandSide().size();
		for (int i = 0; i <= len;) {
			result.add(new LrItem(production.getLeftHandSide(), production.getRightHandSide(), i++));
		}

		return result;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getLeftHandSide());
		sb.append(Symbol.ARROW);
		List<Symbol> pv = getRightHandSide();
		int len = pv.size();
		for (int i = 0; i <= len; ++i) {
			if (i == this.dotPosition) {
				sb.append(Symbol.DOT);
			}
			if (i == len) {
				break;
			}
			sb.append(pv.get(i));
			sb.append(' ');
		}

		return sb.toString();
	}
	
	@Override
	public int hashCode() {
		return super.hashCode() * 101 + getDotPosition() ;
	}

	@Override
	public boolean equals(Object that) {
		if (this == that)
			return true;
		if (that instanceof LrItem) {
			LrItem thatItem = (LrItem) that;
			return thatItem.getDotPosition() == this.getDotPosition()
					&& thatItem.getLeftHandSide().equals(this.getLeftHandSide())
					&& thatItem.getRightHandSide().equals(this.getRightHandSide());
		}
		return false;
	}

	public final int getDotPosition() {
		return dotPosition;
	}

	public static <A extends LrItem> Set<A> getStartingItemForSymbol(Symbol s, Iterable<A> allItems) {
		Set<A> result = new HashSet<A>();
		for (A item : allItems) {
			if (item.getLeftHandSide().equals(s) && item.getDotPosition() == 0) {
				result.add(item);
			}
		}
		return result;
	}

	public static <A extends LrItem> A getItemWithNextDot(A currentItem, Iterable<A> allItems) {
		for (A item : allItems) {
			if (item.getDotPosition() == currentItem.getDotPosition() + 1
					&& item.getLeftHandSide().equals(currentItem.getLeftHandSide())
					&& item.getRightHandSide().equals(currentItem.getRightHandSide())) {
				return item;
			}
		}
		return null;
	}

}
