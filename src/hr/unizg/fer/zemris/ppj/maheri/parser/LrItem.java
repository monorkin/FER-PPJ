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
		}

		return sb.toString();
	}

	public final int getDotPosition() {
		return dotPosition;
	}

}
