package hr.unizg.fer.zemris.ppj.maheri.semantics;

import java.util.List;

import hr.unizg.fer.zemris.ppj.maheri.symbol.Symbol;
import hr.unizg.fer.zemris.ppj.maheri.symbol.TerminalSymbol;

public class TerminalNode extends Node {
	protected final int lineNo;
	protected final String text;

	public TerminalNode(TerminalSymbol symbol, int lineNo, String text) {
		super(symbol);
		this.lineNo = lineNo;
		this.text = text;
	}
	
	@Override
	public List<Node> getChildren() {
		return null;
	}

	public int getLineNo() {
		return lineNo;
	}

	public String getText() {
		return text;
	}

	@Override
	public String toString() {
		return symbol.toString() + "(" + lineNo + "," + text + ")";
	}

}
