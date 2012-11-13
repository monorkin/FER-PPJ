package hr.unizg.fer.zemris.ppj.maheri.parser;

import hr.unizg.fer.zemris.ppj.maheri.automaton.Automaton;

import java.util.HashSet;
import java.util.Set;

public class ParserUtils {

	public static final String START_SYMBOL_FORMAT = "_%s_";
	public static final String TRANSITION_FORMAT = "%s,%s";
	
	/*
	 * A.K.A. skup ZAPOČINJE za nezavršne znakove
	 * TODO tljubej
	 */
	public static Set<String> begins(Grammar grammar, String nonterminalSymbol) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	/*
	 * A.K.A. skup ZAPOČINJE za produkcije
	 * TODO tljubej
	 */
	public static Set<String> begins(Grammar grammar, Production production) {
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	/*
	 * A.K.A. skup slijedi za sve prazne nezavršne znakove
	 * TODO tljubej
	 */
	public static Set<String> follows(Grammar grammar, String nonterminalSymbol) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	public static Automaton automatonFromGrammar(Grammar grammar) {
		Set<Lr1Item> lrItems = new HashSet<Lr1Item>();
		for (Production p : grammar.getProductions()) {
			for (LrItem item : LrItem.fromProduction(p)) {
				lrItems.add(new Lr1Item(item, begins(grammar, item)));
			}
		}
		String startSymbol = grammar.getStartSymbol();
		String newStartSymbol = String.format(START_SYMBOL_FORMAT, startSymbol);
		Lr1Item q0 = new Lr1Item(newStartSymbol, startSymbol, 0);
		Lr1Item q1 = new Lr1Item(newStartSymbol, startSymbol, 1);
		lrItems.add(q0);
		lrItems.add(q1);

		// TODO psegina : generate transition map
		// TODO psegina : transform to DFA
		
		throw new UnsupportedOperationException("Not yet implemented");
	}

}
