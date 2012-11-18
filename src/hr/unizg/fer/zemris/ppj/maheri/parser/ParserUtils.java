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

import java.io.Serializable;
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

		{
			for (LrItem item1 : lrItems) {
				for (LrItem item2 : lrItems) {
					if (item1 != item2 && item1.equals(item2)) {
						System.err.println("Sanity check failed: wrong lritem equals");
						System.err.println(item1 + " ?= " + item2);
					}
					if (item1 != item2 && item1.hashCode() != item2.hashCode() && item1.equals(item2)) {
						System.err.println("Sanity check failed: wrong lritem equals");
						System.err.println(item1 + " ?= " + item2);
					}
				}
			}
		}

		/*
		 * Make start state
		 */
		TerminalSymbol endS = new TerminalSymbol("#END#");
		Set<LrItem> possibleStartItems = LrItem.getStartingItemForSymbol(startSymbol, lrItems);
		Lr1Item start1Item = new Lr1Item(possibleStartItems.toArray(new LrItem[1])[0], new HashSet<TerminalSymbol>(
				Arrays.asList(new TerminalSymbol[] { endS })));

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

		Map<State, Map<String, Transition>> allInOne = new HashMap<State, Map<String, Transition>>();
		
		Set<Lr1Item> doneItems = new HashSet<Lr1Item>();

		int debugNumTran = 0;

		/*
		 * Additional transitions - these must be done after the first pass, as
		 * not all states were available until now
		 */
		boolean changed = true;
		while (changed) {
			Logger.log("\nNew iteration");
			Logger.log("-------------\n");
			changed = false;
			Map<Lr1Item, State> addedStates = new HashMap<Lr1Item, State>();
			for (Entry<Lr1Item, State> pair : autStates.entrySet()) {
				Lr1Item item = pair.getKey();
				State state = pair.getValue();
				if (doneItems.contains(item)) {
					continue;
				}
				Logger.log("\tWorking with state:\n\t\t" + state);

				final Symbol activeSymbol;
				final int dotPosition = item.getDotPosition();
				if (dotPosition < item.getRightHandSide().size()) {
					activeSymbol = item.getRightHandSide().get(dotPosition);
				} else {
					activeSymbol = null;
				}
				Logger.log("\tActive symbol is: " + activeSymbol);

				/*
				 * Rule 4.b - skipping over a symbol
				 */
				LrItem nextItem = LrItem.getItemWithNextDot(item, lrItems);
				Logger.log("\tItem with next dot position is:\n\t\t" + nextItem);
				if (nextItem != null) {
					Symbol x = item.getRightHandSide().get(dotPosition);

					Lr1Item next1Item = new Lr1Item(nextItem, item.getTerminalSymbols());
					State nextState = autStates.get(next1Item);
					State nextState2 = addedStates.get(next1Item);

					boolean stateIsNew = false;

					if (nextState == null && nextState2 == null) {
						nextState = new State(next1Item.toString());
						nextState.setData(next1Item);
						changed = true;
						stateIsNew = true;
						addedStates.put(next1Item, nextState);

						allInOne.put(state, new HashMap<String, Transition>());
					} else if (nextState2 == null) {
						// exists in autStates == old state
						stateIsNew = false;
					} else if (nextState == null) {
						// doesnt exist in autState = new state
						nextState = nextState2;
						stateIsNew = false;
					}
					Transition next = new Transition(state, x.getValue(), Arrays.asList(nextState));
					boolean addedTransition = transitions.add(next);

					Map<String, Transition> map = allInOne.get(state);
					Transition oldNext = map.get(x.getValue());

					if (oldNext != null && !oldNext.equals(next)) {
						System.err.println("INFO: Have similar old nonepsilon transition!!!");
						System.err.println("### " + oldNext + " ### ");
						System.err.println("### " + next + " ### ");
						map.put(x.getValue(), next);
					}

					if (addedTransition) {
						Logger.log("\t---dotskip-----");
						Logger.log("\t-- \tAdding transition = :\n\t-- \t" + next);
						if (stateIsNew) {
							Logger.log("NEW\t++ \tThis is a new state, MARKed it for addition");
						}
						Logger.log("\t---end dotskip---");
					} else {
					}

				} else {
				}

				/*
				 * Rule 4.c - analysis of nonterminal symbols
				 */

				if (activeSymbol != null && !activeSymbol.isTerminal()) {
					int len = item.getRightHandSide().size();
					List<Symbol> remainingSymbols = new ArrayList<Symbol>();
					for (int i = dotPosition + 1; i < len; ++i) {
						remainingSymbols.add(item.getRightHandSide().get(i));
					}
					boolean stringEmpty = isStringEmpty(remainingSymbols);
					Set<TerminalSymbol> t = new HashSet<TerminalSymbol>();
					Set<TerminalSymbol> startsWith = startsWithSet(remainingSymbols);
					t.addAll(startsWith);
					if (stringEmpty) {
						t.addAll(item.getTerminalSymbols());
					}
					
					Set<LrItem> items = LrItem.getStartingItemForSymbol(activeSymbol, lrItems);
					Set<State> epsilonDests = new HashSet<State>();
					
					boolean anyNew = false;
					boolean[][] markerArray = new boolean[2][items.size()];
					int i = 0;
					
					for (LrItem titem : items) {
						Lr1Item new1Item = new Lr1Item(titem, t);
						State newState = autStates.get(new1Item);
						State newState2 = addedStates.get(new1Item);

						boolean stateIsNew = false;
						if (newState == null && newState2 == null) {
							newState = new State(new1Item.toString());
							newState.setData(new1Item);
							changed = true;
							stateIsNew = true;
							addedStates.put(new1Item, newState);
						} else if (newState2 == null) {
							// state is in added
							stateIsNew = false;
						} else if (newState == null) {
							// just added
							newState = newState2;
							stateIsNew = false;
						}
						
						markerArray[0][i++] = stateIsNew;
						if (stateIsNew)
							anyNew = true;
						
						epsilonDests.add(newState);
					}
					Logger.log("\t===lookahead set======");
					Logger.log("\t== Remaining symbols:\n\t===\t" + remainingSymbols);
					Logger.log("\t== \tStarts with: " + startsWith);
					if (stringEmpty) {
						Logger.log("\t== \tRemainder is an empty one, merging with original");
					}
					Logger.log("\t== Nonterminal symbol " + activeSymbol + " on right hand side");
					Logger.log("\t== \tDestination T = \t===\t\t" + t);
					Logger.log("\t===end lookahead set===");
					
					Logger.log("\tFound these items to connect: ");
					i = 0;
					for (LrItem titem : items) {
						Logger.log((markerArray[0][i++] ? "NEW\t\t" : "\t\t" ) + titem);
					}
					
					Transition trans = new Transition(state, Automaton.EPSILON, new ArrayList<State>(epsilonDests));
					transitions.add(trans);
					if (changed && 1 + 1 == 2)
					break;

					Map<String, Transition> map = allInOne.get(state);
					Transition oldNext = map.get(Automaton.EPSILON);

					if (oldNext != null && !oldNext.equals(trans)) {
						System.err.println("INFO: Have similar old epsilon transition!!!");
						System.err.println("### " + oldNext + " ### ");
						System.err.println("### " + trans + " ### ");
						map.put(Automaton.EPSILON, trans);
					}

				}
				doneItems.add(item);
				Logger.log("\n");
			}
			autStates.putAll(addedStates);

		}
		
		int numTran = 0;
		
		for (Transition t : transitions) {
			numTran += t.getDestinations().size();
		}
		
		for (Transition tr : transitions) {
			boolean foundOrigin = false;
			for (State st : autStates.values()) {
				if (tr.getOrigin() == st)
					foundOrigin = true;
			}
			if (!foundOrigin) {
				throw new Error("One of transition origins is not a state " + tr.getOrigin());
			}
		}
		for (Transition tr : transitions) {
			for (State dt : tr.getDestinations()) {
				boolean foundDest = false;
				for (State st : autStates.values()) {
					if (dt == st)
						foundDest = true;
				}
				if (!foundDest) {
					throw new Error("One of transition destinations is not a state " + dt + " for transition " + tr);
				}
			}
		}
		
		
		System.err.println("Made enfa with " + autStates.size() + " states and " + transitions.size()
				+ " (compacted) transitions, noncompacted is " + numTran);
//		throw new Error();

		ArrayList<State> stateList = new ArrayList<State>(autStates.values());
		ArrayList<Transition> transitionsList = new ArrayList<Transition>(transitions);
		Collections.sort(stateList);
		List<State> acceptableStates = stateList;
		return new eNfa(stateList, symbols, transitionsList, startState, acceptableStates);
	}

	@SuppressWarnings("unchecked")
	public ParserTable makeParser() {
		DFA theDFA = DFAConvert.fromENFA(automatonFromGrammar());

		HashMap<State, HashMap<String, Transition>> descr = theDFA.getDescription();

		int stateNum = 0;
		Map<State, Integer> stateNumbers = new HashMap<State, Integer>();
		Logger.log("Checking LRitems in states");
		for (State state : descr.keySet()) {
			stateNumbers.put(state, stateNum++);
			Logger.log("State " + state + " gets " + (stateNum - 1));
		}

		int productionNum = 0;
		Map<Production, Integer> productionNumbers = new HashMap<Production, Integer>();
		for (Production production : grammar.getProductions()) {
			productionNumbers.put(production, productionNum++);
			Logger.log("Production " + production + " gets " + (productionNum - 1));
		}

		ArrayList<HashMap<String, String>> actionsTable = new ArrayList<HashMap<String, String>>(stateNum);
		// <brojStanja, < Znak,imeAKcije > >
		ArrayList<HashMap<String, ArrayList<String>>> aTransitions = new ArrayList<HashMap<String, ArrayList<String>>>(
				productionNum);
		// brojStanja, < L ili R, lijeva Ili desna strana produkcije >
		int parserStartState = 0;

		for (Production production : grammar.getProductions()) {
			HashMap<String, ArrayList<String>> prod = new HashMap<String, ArrayList<String>>();
			prod.put("L", new ArrayList<String>(Arrays.asList(production.getLeftHandSide().toString())));
			ArrayList<String> right = new ArrayList<String>(production.getRightHandSide().size());
			for (Symbol r : production.getRightHandSide()) {
				right.add(r.toString());
			}
			prod.put("R", right);
			aTransitions.add(prod);
		}

		for (int i = 0; i < stateNum; ++i) {
			actionsTable.add(new HashMap<String, String>());
		}

		for (Entry<State, HashMap<String, Transition>> entry : descr.entrySet()) {
			for (Entry<String, Transition> transitionEntry : entry.getValue().entrySet()) {
				State s = entry.getKey();
				State t = transitionEntry.getValue().getDestination();
				String a = transitionEntry.getKey();

				int sIndex = stateNumbers.get(s);
				int tIndex = stateNumbers.get(t);
				Logger.log("s=" + sIndex + ", a=" + a + ", t=" + tIndex);

				// 4a
				if (a.charAt(0) == '<') {
					// NovoStanje[s, A] = Stavi(t) u PPJ, ili samo 't' u UTR
					Logger.log("\tNovoStanje[s,a]=" + tIndex);
					actionsTable.get(sIndex).put(a, Integer.toString(tIndex));
				}
			}
		}

		for (Entry<State, HashMap<String, Transition>> entry : descr.entrySet()) {
			State s = entry.getKey();
			int sIndex = stateNumbers.get(s);

			Logger.log("s=" + sIndex);

			Set<Lr1Item> items = (Set<Lr1Item>) entry.getKey().getData();
			for (Lr1Item item : items) {
				// 6
				if (item.getDotPosition() == 0 && item.getLeftHandSide().equals(grammar.getStartSymbol())) {
					parserStartState = sIndex;
					Logger.log("Found start state, it is " + parserStartState);
				}

				int prodIndex = productionNumbers.get(new Production(item.getLeftHandSide(), item.getRightHandSide()));
				Logger.log("\tprod=" + prodIndex + " [ "
						+ new LrItem(item.getLeftHandSide(), item.getRightHandSide(), item.getDotPosition()) + " ]");

				// prihvati , reduciraj
				if (item.getDotPosition() == item.getRightHandSide().size()) {
					// 3c
					if (item.getLeftHandSide().equals(grammar.getStartSymbol())) {
						// Akcija[s, END] = Prihvati()
						Logger.log("\t\tPrihvati");
						actionsTable.get(sIndex).put("#END#", "Prihvati");
					} else {
						// 3b
						for (TerminalSymbol ai : item.getTerminalSymbols()) {
							String old = actionsTable.get(sIndex).get(ai);
							if (old != null) {
								int oldIndex = Integer.parseInt(old.substring(1));
								if (old.charAt(0) == 'r') {
									if (oldIndex == prodIndex)
										continue;
									System.err.println("Reduciraj/reduciraj nejednoznacnost u " + s + ", " + item
											+ " izmedju " + old + " i " + ("r" + prodIndex)
											+ " rijeseno u korist one s manjim indeksom");
									if (oldIndex < prodIndex)
										continue;
								} else if (old.charAt(0) == 's') {
									System.err.println("Pomakni/reduciraj nejednoznacnost u " + s + ", " + item
											+ " izmedju " + old + " i " + ("r" + prodIndex)
											+ " rijeseno u korist pomakni");
									continue;
								}
							}
							Logger.log("\t\t[s, " + ai + "] = r" + prodIndex);
							// Akcija[s, ai] = Reduciraj(produkcija)
							actionsTable.get(sIndex).put(ai.toString(), "r" + prodIndex);
						}
					}
				}

				// shift
				for (Entry<String, Transition> transitionEntry : entry.getValue().entrySet()) {
					String a = transitionEntry.getKey();
					State t = transitionEntry.getValue().getDestination();
					int tIndex = stateNumbers.get(t);

					Logger.log("\t\ta=" + a + ", t=" + tIndex);

					// 3a
					if (item.getDotPosition() < item.getRightHandSide().size()) {
						Symbol activeSymbol = item.getRightHandSide().get(item.getDotPosition());
						Logger.log("\t\t -->" + activeSymbol + " ?= " + a + " ==== " + activeSymbol.equals(a));
						if (activeSymbol.equals(a) && a.charAt(0) != '<') {
							// Akcija[s, a] = Pomakni(t) u PPJ, ili s't' u UTR,
							// kao
							// shift
							String old = actionsTable.get(sIndex).get(a);
							if (old != null) {
								if (old.charAt(0) == 'r') {
									System.err
											.println("Pomakni/reduciraj nejednoznacnost u " + s + ", " + item
													+ " izmedju " + old + " i " + ("s" + tIndex)
													+ " rijeseno u korist pomakni");
								}
							}
							Logger.log("\t\t[s, a]=" + "s" + tIndex);
							actionsTable.get(sIndex).put(a, "s" + tIndex);
							// pomakni pregazi sve nejednoznacnosti ako ih ima
						}
					}
				}
			}
		}
		HashSet<String> syncStr = new HashSet<String>();
		for (TerminalSymbol syncSymbol : grammar.getSync())
			syncStr.add(syncSymbol.toString());

		return new ParserTable(actionsTable, aTransitions, parserStartState, syncStr);
	}

	public static class ParserTable implements Serializable {
		private static final long serialVersionUID = 4322541186108105988L;

		ArrayList<HashMap<String, String>> actionsTable;
		// <brojStanja, < Znak,imeAKcije > >
		ArrayList<HashMap<String, ArrayList<String>>> productions;
		// brojStanja, < L ili R, lijeva Ili desna strana produkcije >
		HashSet<String> sync;
		int parserStartState;

		/**
		 * @param actionsTable
		 * @param productions
		 * @param parserStartState
		 */
		public ParserTable(ArrayList<HashMap<String, String>> actionsTable,
				ArrayList<HashMap<String, ArrayList<String>>> productions, int parserStartState, HashSet<String> sync) {
			this.actionsTable = actionsTable;
			this.productions = productions;
			this.parserStartState = parserStartState;
			this.sync = sync;
		}

		public ArrayList<HashMap<String, String>> getActionsTable() {
			return actionsTable;
		}

		public int getParserStartState() {
			return parserStartState;
		}

		public ArrayList<HashMap<String, ArrayList<String>>> getProductions() {
			return productions;
		}

		public HashSet<String> getSync() {
			return sync;
		}

		public void print() {
			for (int i = 0; i < actionsTable.size(); ++i)
				System.err.println(actionsTable.get(i));
		}

	}

	/**
	 * Za debag svrhe, ne shvaćati preozbiljno
	 */
	@Override
	public String toString() {
		String str = "    ";
		for (Symbol s : symbols) {
			str += s.getValue() + " ";
		}
		str += "\n";
		for (int i = 0; i < startsWithSymbol[0].length; i++) {
			str += symbols.get(i).getValue() + "  ";
			for (int j = 0; j < startsWithSymbol[0].length; j++) {
				// čupić bi me tuko zbog ovog
				str += startsWithSymbol[i][j] + " ";
			}
			// i ovog
			str += "\n";
		}

		for (Production p : grammar.getProductions()) {
			str += startsWithSet(p.getRightHandSide()) + "\n";
		}

		return str;
	}
}
