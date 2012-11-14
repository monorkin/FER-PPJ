package hr.unizg.fer.zemris.ppj.maheri.grammar;

import java.util.ArrayList;
import java.util.List;

public class Production {
	private static String EPSILON = "$";

	private Symbol leftSide;
	private List<Symbol> rightSide;

	/**
	 * @param leftSide
	 * @param rightSide
	 */
	public Production(Symbol leftSide, List<Symbol> rightSide) {
		this.leftSide = leftSide;
		this.rightSide = rightSide;
	}

	public Production() {
		leftSide = null;
		rightSide = new ArrayList<Symbol>();
	}

	public Production(Symbol leftSide, String rightSideString, List<Symbol> symbols) {
		this.leftSide = leftSide;
		rightSide = new ArrayList<Symbol>();

		String[] rightSideArray = rightSideString.split("\\s+");

		for (String s : rightSideArray) {
			if (s.equals(EPSILON))
				continue;
			rightSide.add(symbols.get(symbols.indexOf(s)));
		}
	}

	/**
	 * @return the leftSide
	 */
	public Symbol getLeftSide() {
		return leftSide;
	}

	/**
	 * @return the rightSide
	 */
	public List<Symbol> getRightSide() {
		return rightSide;
	}

}
