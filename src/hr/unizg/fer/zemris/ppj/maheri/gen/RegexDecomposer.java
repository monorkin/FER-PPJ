package hr.unizg.fer.zemris.ppj.maheri.gen;

import java.util.List;

/**
 * This class works with a regex as input, decomposes it, then notifies
 * listeners how to build the regex from scratch.
 * 
 * More specifically, the regex is first decomposed with respect to parentheses
 * and operator priorities. Then each registered listener is notified by firing
 * a sequence of operations that should be done to reconstruct the regex. The
 * actions are methods in {@link RegexCompositionListener}, and their meanings
 * are documented therein.
 * 
 * @author dosvald
 */
public abstract class RegexDecomposer {
	private String regex;

	// TODO Visitor to traverse regex parse tree VS listener which is fired for
	// events where visitor would be sent ?
	private List<RegexCompositionListener> listeners;

}
/*
 * Ovo ce se koristiti tako da netko napravi rastav regexa po zagradama i
 * operatorima, i onda na temelju tog pozivati metode IAutomatonBuildera 
 */
