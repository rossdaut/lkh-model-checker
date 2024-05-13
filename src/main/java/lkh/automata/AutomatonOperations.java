package lkh.automata;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AutomatonOperations {
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
}
