package hr.unizg.fer.zemris.ppj.maheri.automaton;

import hr.unizg.fer.zemris.ppj.maheri.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class DFAConvert {

	private static HashMap<State, HashMap<String, Transition>> prepareTransitions(eNfa nfa) {
		HashMap<State, HashMap<String, Transition>> ret = new HashMap<State, HashMap<String, Transition>>(nfa
				.getStates().size());

		for (State state : nfa.getStates()) {
			HashMap<String, Transition> forState = new HashMap<String, Transition>();
			for (Transition transition : state.transitions)
				forState.put(transition.getKey(), transition);

			ret.put(state, forState);
		}

		return ret;
	}

	@SuppressWarnings("unchecked")
	public static DFA fromENFA(eNfa eAut) {
		Set<State> dfaStates = new HashSet<State>();
		Set<Transition> dfaTransitions = new HashSet<Transition>();
		
		Set<State> acceptable = new HashSet<State>(eAut.getAcceptableStates());
		Set<State> dfaAcceptables = new HashSet<State>();
		
		Logger.log("Preparing nfa descriptio...");
		HashMap<State, HashMap<String, Transition>> nfaTransitions = prepareTransitions(eAut);
		Logger.log("Prepared automaton description.");
		
		ArrayList<Object> stateData = new ArrayList<Object>();
		int stateDataIndex = 0;
		for (State s : nfaTransitions.keySet()) {
			stateData.add(s.getData());
			++stateDataIndex;
		}
		int i = 0;
		for (State s : nfaTransitions.keySet()) {
			Set<Integer> set = new HashSet<Integer>(stateDataIndex);
			set.add(i);
//			Logger.log("Converted " + s.getData() + " to " + set.toString());
			s.setData(set);
			++i;
		}

		DataMerger merger = new SetIndexMerger();

		LinkedList<Set<State>> configurationQueue = new LinkedList<Set<State>>();
		LinkedList<State> dfaStateQueue = new LinkedList<State>();

		eAut.reset();
		List<State> startConfiguration = eAut.getActiveStates();
		configurationQueue.add(new HashSet<State>(startConfiguration));
		State dfaStartState = mergeStates(configurationQueue.getFirst(), merger);
		dfaStateQueue.add(dfaStartState);
		
		dfaStates.add(dfaStartState);
		for (State state : startConfiguration) {
			if (acceptable.contains(state))
				dfaAcceptables.add(dfaStartState);
		}
		
		Logger.log("ENFA TO DFA CONVERSION");
		Logger.log("=============================================");

		while (!configurationQueue.isEmpty()) {
			Set<State> stateConfiguration = configurationQueue.pop();
			State dfaState = dfaStateQueue.pop();
			
			Logger.log("Expanding for " + dfaState);

			Map<String, Set<State>> nextConfigurationForKey = new HashMap<String, Set<State>>();

			for (State s : stateConfiguration) {
				Logger.log("\tExpanding member " + s);
				Map<String, Transition> forState = nfaTransitions.get(s);
				for (Entry<String, Transition> entry : forState.entrySet()) {
					String key = entry.getKey();
					Logger.log("\t\tFor key '" + key + "' , have destinations: ");
					
					if (Automaton.EPSILON.equals(key)) {
						Logger.log("\t\t[This is epsilon, we don't need this]");
						continue;
					}
					if (entry.getValue().getDestinations().isEmpty()) {
						Logger.log("\t\t[This transition is dead, skipping it]");
						continue;
					}
					Set<State> nextForKey = nextConfigurationForKey.get(key);
					if (nextForKey == null) {
						Logger.log("\t\t[This is first time we use this key, adding entry for it]");
						nextForKey = new HashSet<State>();
						nextConfigurationForKey.put(key, nextForKey);
					}
					for (State destination : entry.getValue().getDestinations()) {
						Logger.log("\t\t\tAdding destination " + destination);
						nextForKey.add(destination);
						if (destination.hasEpsilonTransition()) {
							Logger.log("\t\t\t\tThis destination has epsilon-transitions, expanding it");
							for (State epsDest : destination.eTransition.getDestinations()) {
								nextForKey.add(epsDest);
								Logger.log("\t\t\t\t\t" + epsDest);
							}
						}
					}
				}
			}
			
			for (Entry<String, Set<State> > entry : nextConfigurationForKey.entrySet()) {
				if (entry.getValue().isEmpty())
					Logger.log("WARN: empty configuration " + entry.getValue() + " for key " + entry.getKey());
								
				Set<State> nextConfiguration = entry.getValue();
				
				boolean isAcceptable = false;
				
				for (State tmp : nextConfiguration) {
					if (acceptable.contains(tmp)) {
						isAcceptable = true;
					}
				}
					
				State newDfaState = mergeStates(nextConfiguration, merger);
				
				if (isAcceptable)
					dfaAcceptables.add(newDfaState);
				
				Transition probablyNewTransition = new Transition(dfaState, entry.getKey(), newDfaState);
				
				boolean transitionIsNew = dfaTransitions.add(probablyNewTransition);
				boolean stateIsNew = dfaStates.add(newDfaState);
				
				if (!transitionIsNew && stateIsNew)
					throw new IllegalStateException("Reached new state via old transition, WTF ???");
				
				if (stateIsNew) {
					configurationQueue.addLast(nextConfiguration);
					dfaStateQueue.addLast(newDfaState);
				}
			}
		}
		
		for (State state : dfaStates) {
			Set<Object> original = new HashSet<Object>();
			for (int index : (Set<Integer>)state.getData())
				original.add(stateData.get(index));
			state.setData(original);
		}

		Logger.log("ENFA has " + eAut.getStates().size() + " states and " + eAut.getTransitions().size() + " transitions");
		Logger.log(eAut.getStates());
		Logger.log(eAut.getTransitions());
		Logger.log("DFA would have " + dfaStates.size() + " states and " + dfaTransitions.size() + " transitions");
		Logger.log("Start in ENFA is " + eAut.getStartState() + ", DFA is " + dfaStartState);
		Logger.log("");
		for (State state : dfaStates) {
			Logger.log(state);
		}
		Logger.log("");
		for (Transition t : dfaTransitions)
			Logger.log(t.getOrigin() + "  ++++++  " + t.getKey() + "  ==>  " + t.getDestination());
		
		
		Logger.log("======= END CONVERSION ========");
		
		Logger.log("converted to dfa with " + dfaStates.size() + " states and " + dfaTransitions.size() +"  transitions");
		
		return new DFA(dfaStates, dfaTransitions, dfaAcceptables, dfaStartState);
	}

	private static State mergeStates(Set<State> original, DataMerger merger) {
		List<String> names = new ArrayList<String>(original.size());
		for (State s : original)
			names.add(s.name);
		Collections.sort(names);

		StringBuilder name = new StringBuilder();
		for (String n : names)
			name.append(n + "|");

		State newState = new State(name.toString());

		Iterator<State> it = original.iterator();
		Object o = it.next().getData();
		while (it.hasNext()) {
			State st = it.next();
			o = merger.merge(o, st.getData());
			Logger.log(" " + st);
		}
		
		if (o instanceof Set<?>) {
			newState.setData(o);			
		} else {
			Set<Object> set = new HashSet<Object>();
			set.add(o);
			newState.setData(set);
		}
		return newState;
	}

	interface DataMerger {
		Object merge(Object a, Object b);
	}
	
	static class SetIndexMerger implements DataMerger {
		@SuppressWarnings("unchecked")
		@Override
		public Object merge(Object a, Object b) {
			Set<Integer> as = (Set<Integer>)a;
			Set<Integer> bs = (Set<Integer>)b;
			Set<Integer> ret = new HashSet<Integer>(as);
			ret.addAll(bs);
			return ret;
		}
		
	}

	@SuppressWarnings("unchecked")
	static class SetDataMerger implements DataMerger {
		@Override
		public Object merge(Object a, Object b) {
			Set<Object> ret = new HashSet<Object>();
			if (a instanceof Set<?>) {
				Set<Object> as = (Set<Object>) a;
				ret.addAll(as);
			} else if (a != null) {
				ret.add(a);
			}
			if (b instanceof Set<?>) {
				Set<Object> bs = (Set<Object>) b;
				ret.addAll(bs);
			} else if (b != null) {
				ret.add(b);
			}
			return ret;
		}
	}
}
