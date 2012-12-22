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
		setAttribute(Attribute.PETLJA, false);

	}

	public Symbol getSymbol() {
		return symbol;
	}

	public abstract List<Node> getChildren();

	@Override
	public String toString() {
		return symbol.toString();
	}

	// DONTFIXME null se namjerno provjerava jer ako pukne tu, onda je greska u
	// atributima (i bila je jedna)
	public Object getAttribute(Attribute key) {
		Object ret = attributes.get(key);

		if (ret == null)
			throw new IllegalArgumentException("no such attribute");

		return ret;
	}

	// DONTFIXME null se namjerno provjerava jer ako pukne tu, onda je greska u
	// atributima (i bila je jedna)
	public void setAttribute(Attribute key, Object value) {
		setAttribute(key, value, false);
	}

	public void setAttribute(Attribute key, Object value, boolean recursive) {
		if (value == null) {
			throw new IllegalArgumentException("null attribute value");
		}

		attributes.put(key, value);
		if (recursive) {
			List<Node> children = getChildren();
			if (children != null) {
				for (Node n : children) {
					n.setAttribute(key, value, recursive);
				}
			}
		}
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
