package hr.unizg.fer.zemris.ppj.maheri.semantics;

import hr.unizg.fer.zemris.ppj.maheri.symbol.NonTerminalSymbol;
import hr.unizg.fer.zemris.ppj.maheri.symbol.TerminalSymbol;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Creates generative tree from its textual representation where one line is one
 * node, and single space indentation specifies child nodes
 * 
 * @author dosvald
 * 
 */
public class InputProcessor {

	private Node treeRoot;

	/**
	 * Create input processor from input lines
	 * 
	 * @param inputLines
	 *            list of input lines
	 */
	public InputProcessor(List<String> inputLines) {
		Iterator<String> it = inputLines.iterator();

		treeRoot = new NonterminalNode(new NonTerminalSymbol(it.next()));

		LinkedList<Node> todoStack = new LinkedList<Node>();
		LinkedList<Integer> indents = new LinkedList<Integer>();
		todoStack.push(treeRoot);
		indents.push(0);

		while (it.hasNext()) {
			Node curr = todoStack.element();
			
			String line = it.next();
			// indent level
			int count = 0;
			while (line.charAt(count) == ' ')
				++count;

			// previous nodes with higher indent are done, put them away
			while (count <= indents.element()) {
				todoStack.pop();
				indents.pop();
			}
			// make next node
			Node next = nodeFromString(line.substring(count));
			
			curr = todoStack.element();
			if (next != null) {
				curr.getChildren().add(next);
				todoStack.push(next);
				indents.push(count);
			} else {
				// null means next node is '$', curr has no (more) children so put it away 
				todoStack.pop();
				indents.pop();
			}
		}
		
		assert todoStack.element() == treeRoot;

	}

	private static Node nodeFromString(String s) {
		switch (s.charAt(0)) {
		case '$':
			return null;
		case '<':
			return new NonterminalNode(new NonTerminalSymbol(s));
		default:
			String[] parts = s.split(" ", 3);
			return new TerminalNode(new TerminalSymbol(parts[0]), Integer.parseInt(parts[1]), parts[2]);
		}
	}

	/**
	 * Get the generative tree (similar to syntax tree) described by input
	 * 
	 * @return the generative tree
	 */
	public Node getTree() {
		return treeRoot;
	}

}
