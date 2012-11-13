package hr.unizg.fer.zemris.ppj.maheri.parser;

import hr.unizg.fer.zemris.ppj.maheri.automaton.Automaton;
import hr.unizg.fer.zemris.ppj.maheri.symbol.NonTerminalSymbol;
import hr.unizg.fer.zemris.ppj.maheri.symbol.Symbol;

import java.util.HashSet;
import java.util.Set;

public class ParserUtils {

	public static final String TRANSITION_FORMAT = "%s,%s";
	
	/*
	 * A.K.A. skup ZAPOČINJE za nezavršne znakove
	 * TODO tljubej
	 */
	public static Set<Symbol> begins(Grammar grammar, String nonterminalSymbol) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	/*
	 * A.K.A. skup ZAPOČINJE za produkcije
	 * TODO tljubej
	 */
	public static Set<NonTerminalSymbol> begins(Grammar grammar, Production production) {
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	/*
	 * A.K.A. skup slijedi za sve prazne nezavršne znakove
	 * TODO tljubej
	 */
	public static Set<Symbol> follows(Grammar grammar, String nonterminalSymbol) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	public static Automaton automatonFromGrammar(Grammar grammar) {
		grammar.createAlternateStartSymbol();
		Set<Lr1Item> lrItems = new HashSet<Lr1Item>();
		for (Production p : grammar.getProductions()) {
			for (LrItem item : LrItem.fromProduction(p)) {
				lrItems.add(new Lr1Item(item, begins(grammar, item)));
			}
		}

		// TODO psegina : generate transition map
		// TODO psegina : transform to DFA
		
		throw new UnsupportedOperationException("Not yet implemented");
	}

}
