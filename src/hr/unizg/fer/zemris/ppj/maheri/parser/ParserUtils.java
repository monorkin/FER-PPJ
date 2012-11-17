package hr.unizg.fer.zemris.ppj.maheri.parser;

import hr.unizg.fer.zemris.ppj.maheri.Logger;
import hr.unizg.fer.zemris.ppj.maheri.automaton.Automaton;
import hr.unizg.fer.zemris.ppj.maheri.automaton.DFA;
import hr.unizg.fer.zemris.ppj.maheri.automaton.DFAConvert;
import hr.unizg.fer.zemris.ppj.maheri.automaton.State;
import hr.unizg.fer.zemris.ppj.maheri.automaton.Transition;
import hr.unizg.fer.zemris.ppj.maheri.automaton.eNfa;
import hr.unizg.fer.zemris.ppj.maheri.symbol.Symbol;
import hr.unizg.fer.zemris.ppj.maheri.symbol.TerminalSymbol;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class ParserUtils {

	private Grammar grammar;

	private Set<Symbol> emptySymbols;
	private char[][] startsWithSymbol;

	private List<Production> productions;
	private ArrayList<Symbol> symbols;

	/**
	 * @param grammar
	 */
	public ParserUtils(Grammar grammar) {
		this.grammar = grammar;

		productions = grammar.getProductions();
		symbols = new ArrayList<Symbol>(grammar.getNonterminalSymbols());
		symbols.addAll(grammar.getTerminalSymbols());

		generateSets();
	}

	private void generateSets() {
		startsWithSymbol = new char[symbols.size()][symbols.size()];
		for (int i = 0; i < symbols.size(); i++) {
			for (int j = 0; j < symbols.size(); j++) {
				startsWithSymbol[i][j] = '0';
			}
		}

		emptySymbols = new HashSet<Symbol>();

		// Empty symbols
		for (Production p : productions) {
			if (p.getRightHandSide().isEmpty()) {
				emptySymbols.add(p.getLeftHandSide());
			}
		}

		boolean change = true;
		while (change) {
			change = false;
			for (Production p : productions) {
				boolean allEmpty = true;
				for (Symbol s : p.getRightHandSide()) {
					if (!emptySymbols.contains(s))
						allEmpty = false;
				}
				if (allEmpty) {
					change = emptySymbols.add(p.getLeftHandSide());
				}
			}
		}
		// End empty symbols

		// Starts directly with
		for (Production p : productions) {
			for (Symbol s : p.getRightHandSide()) {
				startsWithSymbol[symbols.indexOf(p.getLeftHandSide())][symbols.indexOf(s)] = '1';
				if (!emptySymbols.contains(s))
					break;
			}
		}
		// End starts directly with

		// Starts with
		for (int i = 0; i < symbols.size(); i++) {
			for (int j = 0; j < symbols.size(); j++) {
				if (i == j) {
					startsWithSymbol[i][j] = '*';
					continue;
				}
				if (startsWithSymbol[i][j] == '1') {
					for (int k = 0; k < symbols.size(); k++) {
						if (startsWithSymbol[j][k] == '1')
							startsWithSymbol[i][k] = '*';
					}
				}
			}
		}
		// End starts with
	}

	/**
	 * Gets "starts with" set for arbitrary string of symbols
	 * 
	 * @param symString
	 * @return
	 */
	public Set<TerminalSymbol> startsWithSet(List<Symbol> symString) {
		Set<TerminalSymbol> symSet = new HashSet<TerminalSymbol>();

		for (Symbol s : symString) {
			symSet.addAll(startsWithSet(s));
			if (!emptySymbols.contains(s))
				break;
		}

		return symSet;
	}

	/**
	 * 
	 * @param symString
	 * @return true if string of symbols contains only empty symbols
	 */
	public boolean isStringEmpty(List<Symbol> symString) {
		for (Symbol s : symString) {
			if (!emptySymbols.contains(s))
				return false;
		}
		return true;
	}

	/**
	 * Gets "starts with" set for a single symbol
	 * 
	 * @param sym
	 * @return
	 */
	public Set<TerminalSymbol> startsWithSet(Symbol sym) {
		int symIndex = symbols.indexOf(sym);
		Set<TerminalSymbol> symSet = new HashSet<TerminalSymbol>();

		for (int i = 0; i < symbols.size(); i++) {
			if (startsWithSymbol[symIndex][i] == '1' || startsWithSymbol[symIndex][i] == '*') {
				if (symbols.get(i).isTerminal())
					symSet.add((TerminalSymbol) symbols.get(i));
			}
		}

		return symSet;
	}

	public eNfa automatonFromGrammar() {
		Logger.log("=========\n\nConverting grammar\n\n" + grammar + "\n=========");
		Symbol oldStart = grammar.getStartSymbol();
		Symbol startSymbol = grammar.createAlternateStartSymbol();
		
		Set<LrItem> lrItems = new HashSet<LrItem>();
		
		Map<Lr1Item, State> autStates = new HashMap<Lr1Item, State>();
		
		/*
		 * Generate LrItems from the grammar
		 */
		Logger.log("Generated LrItems");
		for (Production p : grammar.getProductions()) {
			for (LrItem item : LrItem.fromProduction(p)) {
				lrItems.add(item);
			}
		}
		
		/*
		 * Make start state
		 */
		TerminalSymbol endS = new TerminalSymbol("#END#");
		Set<LrItem> possibleStartItems = LrItem.getStartingItemForSymbol(startSymbol, lrItems);
		Lr1Item start1Item = new Lr1Item(
				possibleStartItems.toArray(new LrItem[1])[0],
				new HashSet<TerminalSymbol>(Arrays.asList(new TerminalSymbol[] { endS })));
		
		State startState = new State(start1Item.toString());
		startState.setData(start1Item);
		
		autStates.put(start1Item, startState);
		
		/*
		 * Generate a list of symbols for the automaton
		 */
		List<String> symbols = new ArrayList<String>();
		for (Symbol s : grammar.getNonterminalSymbols()) {
			symbols.add(s.getValue());
		}
		for (Symbol s : grammar.getTerminalSymbols()) {
			symbols.add(s.getValue());
		}

		Set<Transition> transitions = new HashSet<Transition>();
		
		/*
		 * Additional transitions - these must be done after the first pass, as
		 * not all states were available until now
		 */
		boolean changed = true;
		while (changed) {
			changed = false;
			Map<Lr1Item, State> addedStates = new HashMap<Lr1Item, State>();
			for (Entry<Lr1Item, State> pair : autStates.entrySet()) {
				Lr1Item item = pair.getKey();
				State state = pair.getValue();

				final Symbol activeSymbol;
				final int dotPosition = item.getDotPosition();
				if (dotPosition < item.getRightHandSide().size()) {
					activeSymbol = item.getRightHandSide().get(dotPosition);
				} else {
					activeSymbol = null;
				}
				Logger.log("Item: " + item);
				Logger.log("\tActive symbol " + activeSymbol);

				/*
				 * Rule 4.b - skipping over a symbol
				 */
				LrItem nextItem = LrItem.getItemWithNextDot(item, lrItems);
				if (nextItem != null) {
					Symbol x = item.getRightHandSide().get(dotPosition);
					Logger.log("\tNext item: " + nextItem);
					
					Lr1Item next1Item = new Lr1Item(nextItem, item.getTerminalSymbols());
					State nextState = autStates.get(next1Item);
					if (nextState == null) {
						nextState = new State(next1Item.toString());
						nextState.setData(next1Item);
						changed = true;
						addedStates.put(next1Item, nextState);
					}
					Transition next = new Transition(state, x.getValue(),
							Arrays.asList(new State[] { nextState }));					
					transitions.add(next);
					
					Logger.log("\t\tTransition: " + next);
				} else {
					Logger.log("\tNo next transition");
				}

				/*
				 * Rule 4.c - analysis of nonterminal symbols
				 */

				if (activeSymbol != null && !activeSymbol.isTerminal()) {
					Logger.log("\tNonterminal symbol " + activeSymbol + " on right hand side");
					int len = item.getRightHandSide().size();
					List<Symbol> remainingSymbols = new ArrayList<Symbol>();
					for (int i = dotPosition + 1; i < len; ++i) {
						remainingSymbols.add(item.getRightHandSide().get(i));
					}
					Logger.log("\tRemaining symbols :" + remainingSymbols);
					Set<TerminalSymbol> t = new HashSet<TerminalSymbol>();
//					if (remainingSymbols.size() > 0) {
						Set<TerminalSymbol> startsWith = startsWithSet(remainingSymbols);
						Logger.log("\tAnalyzing aplicable terminal symbols:");
						Logger.log("\t\tStarts with: " + startsWith);
						t.addAll(startsWith);
						if (isStringEmpty(remainingSymbols)) {
							Logger.log("\t\tRemainder is an empty one.");
							Logger.log("\t\t\tAdding: " + item.getTerminalSymbols());
							t.addAll(item.getTerminalSymbols());
						}
						Logger.log("\tDestination T = " + t);

						Logger.log("\tInitial productions for " + activeSymbol);
						Set<LrItem> items = LrItem.getStartingItemForSymbol(activeSymbol, lrItems);
						for (LrItem titem : items) {
							Logger.log("\t\t" + titem);
						}

						Logger.log("\tNew productions:");
						
						Set<State> epsilonDests = new HashSet<State>();
						for (LrItem titem : items) {
							Lr1Item new1Item = new Lr1Item(titem, t);
							Logger.log("\t\t\t" + new1Item);
							
							State newState = autStates.get(new1Item);
							if (newState == null) {
								Logger.log("\t\t\t\tAdding to current items");
								newState = new State(new1Item.toString());
								newState.setData(new1Item);
								changed = true;
								addedStates.put(new1Item, newState);
							}
							Logger.log("Adding transition to "+newState);
							epsilonDests.add(newState);
						}
						
						Transition trans = new Transition(state, Automaton.EPSILON, new ArrayList<State>(epsilonDests));
						transitions.add(trans);
//					}

				}
			}
			autStates.putAll(addedStates);

		}

		Logger.log("Final transitions:");
		for (Transition t : transitions) {
			Logger.log("\t" + t);
		}
		ArrayList<State> stateList = new ArrayList<State>(autStates.values());
		ArrayList<Transition> transitionsList = new ArrayList<Transition>(transitions);
		Collections.sort(stateList);
		List<State> acceptableStates = stateList;
		return new eNfa(stateList, symbols, transitionsList, startState, acceptableStates);
	}
	
	public void makeParser() {
		DFA theDFA = DFAConvert.fromENFA(automatonFromGrammar());
		
		HashMap<State, HashMap<String, Transition>> descr = theDFA.getDescription();
		
		State parserStart;
		
		int stateNum = 0;
		Map<State, Integer> stateNumbers = new HashMap<State, Integer>();
		Logger.log("Checking LRitems in states");
		for (State state : descr.keySet()) {
			stateNumbers.put(state, ++stateNum); 
			Logger.log("State " + state + " has items " + state.getData());
		}
		
		int productionNum = 0;
		Map<Production, Integer> productionNumbers = new HashMap<Production, Integer>();
		for (Production production : grammar.getProductions()) {
			productionNumbers.put(production, ++productionNum);
		}
		
		Map<Integer, Map <String, String>> actionsTable; // <brojStanja, < Znak, imeAKcije > > 
		Map<Integer, Map <String, List<String>>> aTransitions;  // brojStanja, < L ili R, lijeva Ili desna strana produkcije >
		
		// 3a
		
		// 3b
		
		// 3c
	}
	
	/**
	 * Za debag svrhe, ne shvaćati preozbiljno
	 */
	@Override
	public String toString() {
		String str="    ";
		for (Symbol s: symbols) {
			str+=s.getValue()+" ";
		}
		str+="\n";
		for (int i=0; i<startsWithSymbol[0].length; i++) {
			str+=symbols.get(i).getValue()+"  ";
			for (int j=0; j<startsWithSymbol[0].length; j++) {
				//čupić bi me tuko zbog ovog
				str+=startsWithSymbol[i][j]+" ";
			}
			//i ovog
			str+="\n";
		}
		
		for (Production p: grammar.getProductions()) {
			str+=startsWithSet(p.getRightHandSide())+"\n";
		}
		
		return str;
	}
}
