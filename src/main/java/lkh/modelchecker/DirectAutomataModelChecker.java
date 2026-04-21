package lkh.modelchecker;

import lkh.automata.impl.AutomataOperations;
import lkh.automata.impl.GraphDeterministicAutomaton;
import lkh.expression.Expression;
import lkh.lts.LTS;
import lombok.NonNull;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

public class DirectAutomataModelChecker<State, Action> extends AutomataModelChecker<State, Action> {
  public DirectAutomataModelChecker(@NonNull LTS<State, Action> lts, @NonNull State pointedState) {
    super(lts, pointedState);
  }

  @Override
  protected GraphDeterministicAutomaton<Integer, Action> buildKhAutomaton(Expression initExpr, Expression endExpr) {
    Set<State> initialStates = statesHolding(initExpr);
    Set<State> goalStates = statesHolding(endExpr);

    GraphDeterministicAutomaton<Set<State>, Action> automaton = new GraphDeterministicAutomaton<>();
    Queue<Set<State>> unvisited = new ArrayDeque<>();
    Set<Set<State>> visited = new HashSet<>();

    automaton.setInitialState(initialStates);
    unvisited.add(initialStates);

    while (!unvisited.isEmpty()) {
      Set<State> states = unvisited.remove();
      if (!visited.add(states)) {
        continue;
      }

      if (goalStates.containsAll(states)) {
        automaton.addFinalState(states);
      }

      for (Action action : lts.getActions()) {
        lts.targets(states, action, true).ifPresent(target -> {
          automaton.addTransition(states, target, action);
          if (!visited.contains(target)) {
            unvisited.add(target);
          }
        });
      }
    }

    return AutomataOperations.toIntegerStates(automaton);
  }
}
