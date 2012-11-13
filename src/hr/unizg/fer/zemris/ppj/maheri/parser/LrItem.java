package hr.unizg.fer.zemris.ppj.maheri.parser;

import hr.unizg.fer.zemris.ppj.maheri.symbol.NonTerminalSymbol;
import hr.unizg.fer.zemris.ppj.maheri.symbol.Symbol;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LrItem extends Production {

	private final int dotPosition;

	public LrItem(final NonTerminalSymbol symbol, final List<List<Symbol>> production, final int dotPosition) {
		super(symbol, production);
		if (production.size() > 1) {
			throw new IllegalArgumentException(
					"LrItems can only be constructed from a production with a single element!");
		} else if (dotPosition > production.get(0).size() || dotPosition < 0) {
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

		if (production.getValue().size() == 0) {
			result.add(new LrItem(production.getKey(), production.getValue(), 0));
		}

		for (List<Symbol> prod : production.getValue()) {
			List<List<Symbol>> l = new ArrayList<List<Symbol>>(1);
			l.add(prod);
			int len = prod.size();
			for (int i = 0; i <= len;) {
				result.add(new LrItem(production.getKey(), l, i++));
			}
		}

		return result;
	}

	/**
	 * Convenience method for accessing the value of this production, as it may
	 * only have one
	 * 
	 * @return first element of getValue()
	 */
	public List<Symbol> getProductionValue() {
		return getValue().get(0);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getKey());
		sb.append(Symbol.ARROW);
		List<Symbol> pv = getProductionValue();
		int len = pv.size();
		for (int i = 0; i <= len; ++i) {
			if (i == this.dotPosition) {
				sb.append(Symbol.DOT);
			}
			if (i == len) {
				break;
			}
			sb.append(pv.get(i));
		}

		return sb.toString();
	}

	public final int getDotPosition() {
		return dotPosition;
	}

}
