package hr.unizg.fer.zemris.ppj.maheri.parser;

import java.util.HashSet;
import java.util.Set;

public class LrItem extends Production {

	public static final String DOT = "*";

	private final int dotPosition;

	public LrItem(final String symbol, final String production, final int dotPosition) {
		super(symbol, production);
		if (dotPosition > production.length() || dotPosition < 0) {
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
		String[] productions = production.getValue().split("\\|");
		for (String s : productions) {
			int len = s.length();
			for (int i = 0; i <= len; ++i) {
				result.add(new LrItem(production.getKey(), s, i));
			}
		}
		return result;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		StringBuilder production = new StringBuilder(getValue());
		production.insert(dotPosition, DOT);
		sb.append(getKey());
		sb.append("->");
		sb.append(production);
		return sb.toString();
	}

	public final int getDotPosition() {
		return dotPosition;
	}

}
