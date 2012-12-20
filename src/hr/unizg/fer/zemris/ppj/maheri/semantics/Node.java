package hr.unizg.fer.zemris.ppj.maheri.semantics;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hr.unizg.fer.zemris.ppj.maheri.symbol.Symbol;

public abstract class Node {
	protected final Symbol symbol;

	protected Map<Attribute, Object> attributes;

	/**
	 * Create node in syntax/generative tree from its grammar symbol
	 * 
	 * @param symbol
	 *            syntax-related symbol of this node
	 * @param lineNo
	 *            line number for this
	 * @param text
	 */
	public Node(Symbol symbol) {
		super();
		attributes = new HashMap<Node.Attribute, Object>();
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

	public Object getAttribute(Attribute key) {
		Object ret = attributes.get(key);

		if (ret == null)
			throw new IllegalArgumentException("no such attribute");
		return ret;
	}

	public void setAttribute(Attribute key, Object value) {
		if (value == null) {
			throw new IllegalArgumentException("null attribute value");
		}
		attributes.put(key, value);
	}

	enum Attribute {
		// variables, expressions
		IME, TIP, L_IZRAZ,

		// arg-lists and param-lists
		TIPOVI, IMENA,

		// initializers, arrays
		NTIP, BR_ELEM,

		// moj
		PETLJA,

	}

}
