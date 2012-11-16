package hr.unizg.fer.zemris.ppj.maheri.automaton;

import hr.unizg.fer.zemris.ppj.maheri.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class DFAConvert {

	public static void dfaFromENFA(eNfa eAut) {
		Set<String> dfaSymbols = new HashSet<String>(eAut.getSymbols());
		Set<State> dfaStates = new HashSet<State>();
		Set<Transition> dfaTransitions = new HashSet<Transition>();
		
		DataMerger merger = new SetDataMerger();

		LinkedList<Set<State>> todo = new LinkedList<Set<State>>();

		eAut.reset();

		todo.add(new HashSet<State>(eAut.getActiveStates()));

		while (!todo.isEmpty()) {
			Set<State> stateConfiguration = todo.getFirst();
			State dfaState = mergeStates(stateConfiguration, merger);
			
			for (String str : dfaSymbols) {
				Set<State> nextConfiguration = new HashSet<State>();
				for (State s : stateConfiguration) {
					nextConfiguration.addAll(eAut.peekNextChar(str));
				}
				State potentiallyNewState = mergeStates(nextConfiguration, merger);
				Transition probablyNewTransition = new Transition(dfaState, str, potentiallyNewState);
				
				boolean transitionIsNew = dfaTransitions.add(probablyNewTransition);
				boolean stateIsNew = dfaStates.add(potentiallyNewState);
				
				if (!transitionIsNew && stateIsNew)
					throw new IllegalStateException("New state via old transition ???");
				
				if (stateIsNew)
					todo.addLast(nextConfiguration);
			}
		}
		
		Logger.log("DFA would have " + dfaStates.size() + " and " + dfaTransitions.size() + " transitions");
	}

	private static State mergeStates(Set<State> original, DataMerger merger) {
		List<String> names = new ArrayList<String>(original.size());
		for (State s : original)
			names.add(s.name);
		Collections.sort(names);

		StringBuilder name = new StringBuilder();
		for (String n : names)
			name.append(n);

		State newState = new State(name.toString());

		Iterator<State> it = original.iterator();
		Object o = it.next().getData();
		while (it.hasNext()) {
			o = merger.merge(o, it.next().getData());
		}

		newState.setData(o);
		return newState;
	}

	interface DataMerger {
		Object merge(Object a, Object b);
	}

	@SuppressWarnings("unchecked")
	static class SetDataMerger implements DataMerger {
		@Override
		public Object merge(Object a, Object b) {
			Set<Object> ret = new HashSet<Object>();
			if (a instanceof Set<?>) {
				Set<Object> as = (Set<Object>) a;
				ret.addAll(as);
			} else {
				ret.add(a);
			}
			if (b instanceof Set<?>) {
				Set<Object> bs = (Set<Object>) b;
				ret.addAll(bs);
			} else {
				ret.add(b);
			}
			return ret;
		}
	}
}
