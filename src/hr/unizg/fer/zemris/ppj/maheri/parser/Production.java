package hr.unizg.fer.zemris.ppj.maheri.parser;

import hr.unizg.fer.zemris.ppj.maheri.symbol.NonTerminalSymbol;
import hr.unizg.fer.zemris.ppj.maheri.symbol.Symbol;

import java.util.List;

public class Production {

	private final NonTerminalSymbol key;
	private final List<List<Symbol>> value;

	public NonTerminalSymbol getKey() {
		return key;
	}

	public List<List<Symbol>> getValue() {
		return value;
	}

	public Production(final NonTerminalSymbol key, final List<List<Symbol>> value) {
		this.key = key;
		this.value = value;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(key);
		sb.append(Symbol.ARROW);
		if (value.size() > 0) {
			for (List<Symbol> production : value) {
				for (Symbol s : production) {
					sb.append(s);
				}
				sb.append("|");
			}
			sb.setLength(sb.length() - 1);
		}
		return sb.toString();
	}

}
