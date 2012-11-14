package hr.unizg.fer.zemris.ppj.maheri.parser;

import hr.unizg.fer.zemris.ppj.maheri.symbol.NonTerminalSymbol;
import hr.unizg.fer.zemris.ppj.maheri.symbol.Symbol;

import java.util.ArrayList;
import java.util.List;

public class Production {
	protected static final String EPSILON = "$"; 

	private final NonTerminalSymbol leftSide;
	private final List<Symbol> rightSide;

	public NonTerminalSymbol getLeftHandSide() {
		return leftSide;
	}

	public List<Symbol> getRightHandSide() {
		return rightSide;
	}

	public Production(final NonTerminalSymbol key, final List<Symbol> value) {
		this.leftSide = key;
		this.rightSide = value;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(leftSide);
		sb.append(Symbol.ARROW);
		if (rightSide.size() > 0) {
			for (Symbol s : rightSide) {
				sb.append(s);
			}
		} else {
			sb.append(EPSILON);
		}
		return sb.toString();
	}



}
