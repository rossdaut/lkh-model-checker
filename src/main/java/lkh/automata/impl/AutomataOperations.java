package lkh.automata.impl;

import lkh.utils.MarkableSet;
import lkh.utils.Pair;

import java.util.*;
import java.util.stream.Collectors;

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
  public static <State, Symbol> GraphDeterministicAutomaton<Integer, Symbol> determinize(GraphNonDeterministicAutomaton<State, Symbol> automaton) {
    GraphDeterministicAutomaton<Integer, Symbol> result = new GraphDeterministicAutomaton<>();
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
   * Minimize a DFA.
   * The resulting DFA has states of type Integer. The content of the states of the input automaton is not preserved.
   * The symbols will remain the same type.
   * @param automaton a DFA
   * @return a DFA accepting the same language as the input DFA with minimum number of states
   * @param <State> the type of State of the input DFA
   * @param <Symbol> the type of Symbol
   */
  public static <State, Symbol> GraphDeterministicAutomaton<Integer, Symbol> minimize(GraphDeterministicAutomaton<State, Symbol> automaton) {
    GraphDeterministicAutomaton<Integer, Symbol> result = new GraphDeterministicAutomaton<>();
    Set<Set<State>> P = quotientSet(automaton);
    Map<Set<State>, Integer> indexMap = new HashMap<>();

    for(Set<State> X : P) {
      indexMap.putIfAbsent(X, indexMap.size());
      result.addState(indexMap.get(X));

      if (X.contains(automaton.initialState))
        result.setInitialState(indexMap.get(X));

      if (X.stream().anyMatch(automaton::isFinal))
        result.addFinalState(indexMap.get(X));

      for (Symbol symbol : automaton.getAlphabet()) {
        Optional<State> target = automaton.delta(X.stream().findAny().get(), symbol);
        if (target.isEmpty()) continue;

        Set<State> Y = P.stream().filter(x -> x.contains(target.get())).findAny().get();
        indexMap.putIfAbsent(Y, indexMap.size());
        result.addTransition(indexMap.get(X), indexMap.get(Y), symbol);
      }
    }

    return result;
  }

  private static <State, Symbol> Set<Set<State>> quotientSet(GraphDeterministicAutomaton<State, Symbol> automaton) {
    Set<MarkableSet<State>> P = new HashSet<>();
    Set<MarkableSet<State>> P2 = new HashSet<>();
    MarkableSet<State> X2;
    boolean changed = true;

    P.add(new MarkableSet<>(automaton.getNonFinalStates()));
    P.add(new MarkableSet<>(automaton.getFinalStates()));

    while (changed) {
      for (MarkableSet<State> X : P) {
        for (State e : X.getUnmarkedElements()) {
          if (X.isMarked(e)) continue;

          X.mark(e);
          X2 = new MarkableSet<>(Collections.singleton(e));

          for (State e2 : X.getUnmarkedElements()) {
            if (equivalent(automaton, e, e2, P)) {
              X2.add(e2);
              X.mark(e2);
            }
          }

          P2.add(X2);
        }
      }
      if (P.equals(P2)) {
        changed = false;
      } else {
        P = P2;
        P2 = new HashSet<>();
      }
    }

    return P.stream().map(MarkableSet::getElements).collect(Collectors.toSet());
  }

  private static <State, Symbol> boolean equivalent(GraphDeterministicAutomaton<State, Symbol> automaton, State e, State e2, Set<MarkableSet<State>> P) {
    for(Symbol symbol : automaton.getAlphabet()) {
      Optional<State> t1 = automaton.delta(e, symbol);
      Optional<State> t2 = automaton.delta(e2, symbol);
      if (t1.isEmpty() && t2.isEmpty()) continue;
      if (t1.isEmpty() || t2.isEmpty()) return false;

      for(MarkableSet<State> X : P) {
        if (X.contains(t1.get()) && !X.contains(t2.get())) {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * NFA intersection.
   * @param automaton1 a complete NFA
   * @param automaton2 a complete NFA
   * @return a NFA accepting the intersection of the languages of the input NFAs
 */
  public static <A, B, Symbol> GraphNonDeterministicAutomaton<Pair<A, B>, Symbol>
  intersection(GraphNonDeterministicAutomaton<A, Symbol> automaton1, GraphNonDeterministicAutomaton<B, Symbol> automaton2) {
    //Chequear que estén completos???
    GraphNonDeterministicAutomaton<Pair<A, B>, Symbol> result = new GraphNonDeterministicAutomaton<>();
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
  public static <A, B, Symbol> GraphDeterministicAutomaton<Integer, Symbol>
  intersection(GraphDeterministicAutomaton<A, Symbol> automaton1, GraphDeterministicAutomaton<B, Symbol> automaton2) {
    GraphDeterministicAutomaton<Integer, Symbol> result = new GraphDeterministicAutomaton<>();
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
  public static <State, Symbol> GraphDeterministicAutomaton<Integer, Symbol>
  intersection(Set<GraphDeterministicAutomaton<State, Symbol>> automata) {
    if (automata == null) throw new NullPointerException("null automata set");
    if (automata.isEmpty()) { throw new IllegalArgumentException("empty automata set"); }

    Queue<GraphDeterministicAutomaton<State, Symbol>> queue = new LinkedList<>(automata);
    GraphDeterministicAutomaton<Integer, Symbol> result = toIntegerStates(queue.remove());

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
  public static <State, Symbol> GraphDeterministicAutomaton<State, Symbol>
  asDeterministic(GraphNonDeterministicAutomaton<State, Symbol> nfa) {
    GraphDeterministicAutomaton<State, Symbol> dfa = new GraphDeterministicAutomaton<>();
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
  public static <State, Symbol> GraphDeterministicAutomaton<State, Symbol>
  complement(GraphDeterministicAutomaton<State, Symbol> dfa) {
    GraphDeterministicAutomaton<State, Symbol> result = dfa.clone();
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
  public static <State, Symbol> GraphDeterministicAutomaton<Integer, Symbol>
  toIntegerStates(GraphDeterministicAutomaton<State, Symbol> automaton) {
    GraphDeterministicAutomaton<Integer, Symbol> result = new GraphDeterministicAutomaton<>();
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
