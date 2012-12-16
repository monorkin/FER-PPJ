package hr.unizg.fer.zemris.ppj.maheri.semantics;

import java.util.ArrayList;
import java.util.List;

import hr.unizg.fer.zemris.ppj.maheri.symbol.NonTerminalSymbol;

public class NonterminalNode extends Node {
	
	protected final List<Node> children = new ArrayList<Node>();

	public NonterminalNode(NonTerminalSymbol symbol) {
		super(symbol);
	}

	@Override
	public List<Node> getChildren() {
		return children;
	}
	

}
