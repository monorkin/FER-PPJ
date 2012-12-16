package hr.unizg.fer.zemris.ppj.maheri.semantics;

import java.util.ArrayList;
import java.util.List;

import hr.unizg.fer.zemris.ppj.maheri.symbol.Symbol;

public abstract class Node {
	protected final Symbol symbol;
	
	/**
	 * Create node in syntax/generative tree from its grammar symbol
	 * @param symbol syntax-related symbol of this node
	 * @param lineNo line number for this 
	 * @param text
	 */
	public Node(Symbol symbol) {
		super();
		this.symbol = symbol;
		
		
	}

	public Symbol getSymbol() {
		return symbol;
	}

	public abstract List<Node> getChildren();
	
	@Override
	public String toString() {
		return symbol.toString();
	}
	
	public Object getAttribute(Object key) {
		throw new UnsupportedOperationException("unimplemented");
	}
	
	public Object setAttribute(Object key) {
		throw new UnsupportedOperationException("unimplemented");
	}

}
