package lkh.automata;

import lkh.utils.Pair;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class AutomataOperations {
  /**
   * Determinizes a NFA.
   * The resulting DFA has states of type Integer. The content of the states of the input automaton is not preserved.
   * The symbols will remain the same type.
   * @param automaton a NFA
   * @return a DFA accepting the same language as the input NFA
   * @param <State> the type of State of the input NFA
   * @param <Symbol> the type of Symbol
   */
  public static <State, Symbol> DeterministicAutomaton<Integer, Symbol> determinize(NonDeterministicAutomaton<State, Symbol> automaton) {
    DeterministicAutomaton<Integer, Symbol> result = new DeterministicAutomaton<>();
    Set<Set<State>> unvisitedStates = new HashSet<>();
    Map<Set<State>, Integer> indexMap = new HashMap<>();
    Set<State> s, m;
    int lastIndex = 0;

    s = automaton.lambdaClosure(automaton.initialState);
    indexMap.put(s, lastIndex++);
    result.setInitialState(0);
    unvisitedStates.add(s);

    while (!unvisitedStates.isEmpty()) {
      s = unvisitedStates.stream().findAny().get();
      unvisitedStates.remove(s);

      for (Symbol symbol : automaton.alphabet) {
        m = automaton.lambdaClosure(automaton.move(s, symbol));
        // comment the following for complete automaton
        if (m.isEmpty()) {
          continue;
        }

        if (!indexMap.containsKey(m)) {
          unvisitedStates.add(m);
          indexMap.put(m, lastIndex++);

          if (automaton.finalStates.stream().anyMatch(m::contains))
            result.addFinalState(indexMap.get(m));
        }

        result.addTransition(indexMap.get(s), indexMap.get(m), symbol);
      }
    }

    return result;
  }

  /**
   * NFA intersection.
   * @param automaton1 a complete NFA
   * @param automaton2 a complete NFA
   * @return a NFA accepting the intersection of the languages of the input NFAs
 */
  public static <A, B, Symbol> NonDeterministicAutomaton<Pair<A, B>, Symbol>
  intersection(NonDeterministicAutomaton<A, Symbol> automaton1, NonDeterministicAutomaton<B, Symbol> automaton2) {
    //Chequear que estén completos???
    NonDeterministicAutomaton<Pair<A, B>, Symbol> result = new NonDeterministicAutomaton<>();
    Set<Pair<A, B>> unvisitedStates = new HashSet<>();

    // Initial state
    Pair<A, B> initial = new Pair<>(automaton1.initialState, automaton2.initialState);
    unvisitedStates.add(initial);
    result.setInitialState(initial);

    // Transition map
    while(!unvisitedStates.isEmpty()) {
      Pair<A, B> pair = unvisitedStates.stream().findAny().get();
      unvisitedStates.remove(pair);

      for (Symbol symbol : automaton1.alphabet) {
        Set<A> s1 = automaton1.lambdaClosure(automaton1.delta(pair.key(), symbol));
        Set<B> s2 = automaton2.lambdaClosure(automaton2.delta(pair.value(), symbol));

        for (A state1 : s1) {
          for (B state2 : s2) {
            Pair<A, B> next = new Pair<>(state1, state2);
            if (!result.getStates().contains(next)) {
              unvisitedStates.add(next);
              result.addState(next);

              if (automaton1.isFinal(state1) && automaton2.isFinal(state2)) {
                result.addFinalState(next);
              }
            }

            result.addTransition(pair, next, symbol);
          }
        }
      }
    }

    return result;
  }

  /**
   * DFA intersection.
   * @param automaton1 a DFA
   * @param automaton2 a DFA
   * @return a NFA accepting the intersection of the languages of the input DFAs
   */
  public static <A, B, Symbol> DeterministicAutomaton<Integer, Symbol>
  intersection(DeterministicAutomaton<A, Symbol> automaton1, DeterministicAutomaton<B, Symbol> automaton2) {
    DeterministicAutomaton<Integer, Symbol> result = new DeterministicAutomaton<>();
    Set<Pair<A, B>> unvisitedStates = new HashSet<>();
    Map<Pair<A, B>, Integer> indexMap = new HashMap<>();
    int lastIndex = 0;

    // Initial state
    Pair<A, B> initial = new Pair<>(automaton1.initialState, automaton2.initialState);
    indexMap.put(initial, lastIndex++);
    unvisitedStates.add(initial);
    result.setInitialState(indexMap.get(initial));

    // Transition map
    while(!unvisitedStates.isEmpty()) {
      Pair<A, B> pair = unvisitedStates.stream().findAny().get();
      unvisitedStates.remove(pair);

      if (automaton1.isFinal(pair.key()) && automaton2.isFinal(pair.value())) {
        result.addFinalState(indexMap.get(pair));
      }

      for (Symbol symbol : automaton1.getAlphabet()) {
        Optional<A> s1 = automaton1.delta(pair.key(), symbol);
        Optional<B> s2 = automaton2.delta(pair.value(), symbol);

        if (s1.isEmpty() || s2.isEmpty()) continue;

        Pair<A, B> next = new Pair<>(s1.get(), s2.get());

        if (!indexMap.containsKey(next)) {
          unvisitedStates.add(next);
          indexMap.put(next, lastIndex++);
        }

        result.addTransition(indexMap.get(pair), indexMap.get(next), symbol);
      }
    }

    return result;
  }

  /**
   * Set of DeterministicAutomaton intersection.
   * @param automata a set of DFAs
   * @return a DFA accepting the intersection of the languages of all DFA's
   * @param <Symbol> the type of the symbols
   */
  public static <State, Symbol> DeterministicAutomaton<Integer, Symbol>
  intersection(Set<DeterministicAutomaton<State, Symbol>> automata) {
    if (automata == null) throw new NullPointerException("null automata set");
    if (automata.isEmpty()) { throw new IllegalArgumentException("empty automata set"); }

    Queue<DeterministicAutomaton<State, Symbol>> queue = new LinkedList<>(automata);
    DeterministicAutomaton<Integer, Symbol> result = toIntegerStates(queue.remove());

    while (!queue.isEmpty()) {
      result = intersection(result, queue.remove());
    }

    return result;
  }

  /**
   * NonDeterministicAutomaton to DeterministicAutomaton passage.
   * It checks that the input has a deterministic structure.
   * @param nfa a NFA that can be automatically determinized
   * @return the same automaton as a DeterministicAutomaton
   * @throws IllegalStateException if the input is not directly determinizable
   */
  public static <State, Symbol> DeterministicAutomaton<State, Symbol>
  asDeterministic(NonDeterministicAutomaton<State, Symbol> nfa) {
    DeterministicAutomaton<State, Symbol> dfa = new DeterministicAutomaton<>();
    Set<State> nextStates;

    dfa.setInitialState(nfa.getInitialState());

    for (State state : nfa.getStates()) {
      if (!nfa.emptyDelta(state).isEmpty())
        throw new IllegalStateException("the NFA cannot be directly determinized (has empty transitions)");

      for (Symbol symbol : nfa.getAlphabet()) {
        nextStates = nfa.delta(state, symbol);
        if (nextStates.size() > 1)
          throw new IllegalStateException("the NFA cannot be directly determinized (has multiple transitions)");

        nextStates.stream().findFirst().ifPresent(target -> dfa.addTransition(state, target, symbol));
      }
    }

    for (State state : nfa.getFinalStates()) {
      dfa.addFinalState(state);
    }

    return dfa;
  }

  /**
   * Complements a DFA.
   * @param dfa a complete DFA
   * @return a DFA accepting the complement language of the input DFA
   * @param <State> the type of State of the input DFA
   * @param <Symbol> the type of Symbol
   */
  public static <State, Symbol> DeterministicAutomaton<State, Symbol>
  complement(DeterministicAutomaton<State, Symbol> dfa) {
    DeterministicAutomaton<State, Symbol> result = dfa.clone();
    result.finalStates.addAll(dfa.getStates());
    result.finalStates.removeAll(dfa.finalStates);
    return result;
  }

  /**
   * Return an equivalent automaton where states are replaced for integers.
   * @param automaton a non-null deterministic automaton
   * @return a deterministic automaton with integer states
   * @param <State> the type of the input automaton states
   * @param <Symbol> the type of the symbols
   */
  static private <State, Symbol> DeterministicAutomaton<Integer, Symbol>
  toIntegerStates(DeterministicAutomaton<State, Symbol> automaton) {
    DeterministicAutomaton<Integer, Symbol> result = new DeterministicAutomaton<>();
    Map<State, Integer> indexMap = new HashMap<>();

    for (State state: automaton.getStates()) {
      indexMap.put(state, indexMap.size());
      result.addState(indexMap.get(state));

      if (automaton.isFinal(state)) {
        result.addFinalState(indexMap.get(state));
      }
    }

    result.initialState = indexMap.get(automaton.getInitialState());

    for(State source: automaton.getStates()) {
      for(Symbol symbol: automaton.getAlphabet()) {
        Optional<State> target = automaton.delta(source, symbol);
        target.ifPresent(t -> result.addTransition(indexMap.get(source), indexMap.get(t), symbol));
      }
    }

    return result;
  }
}
