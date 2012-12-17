package hr.unizg.fer.zemris.ppj.maheri.semantics;

import java.util.List;

import hr.unizg.fer.zemris.ppj.maheri.symbol.TerminalSymbol;

/**
 * Corresponds to terminal symbols in a syntax tree
 * 
 * @author dosvald
 */
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

	/**
	 * @return line number in source file where this node's symbol is found
	 */
	public int getLineNo() {
		return lineNo;
	}

	/**
	 * @return original text from source. Example: if this node represents a
	 *         variable, text is a variable's name; if this node represents a
	 *         keyword, text would be keyword string.
	 */
	public String getText() {
		return text;
	}

	@Override
	public String toString() {
		return symbol.toString() + "(" + lineNo + "," + text + ")";
	}

}
