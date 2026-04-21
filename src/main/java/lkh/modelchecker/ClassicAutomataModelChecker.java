package lkh.modelchecker;

import lkh.automata.impl.AutomataOperations;
import lkh.automata.impl.GraphDeterministicAutomaton;
import lkh.expression.Expression;
import lkh.lts.LTS;
import lombok.Getter;
import lombok.NonNull;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

public class ClassicAutomataModelChecker<State, Action> extends AutomataModelChecker<State, Action> {
  @Getter private boolean minimize;

  public ClassicAutomataModelChecker(@NonNull LTS<State, Action> lts, @NonNull State pointedState, boolean minimize) {
    super(lts, pointedState);
    this.minimize = minimize;
  }

  public ClassicAutomataModelChecker(@NonNull LTS<State, Action> lts, @NonNull State pointedState) {
    this(lts, pointedState, false);
  }

  public void setMinimize(boolean minimize) {
    if (this.minimize == minimize) {
      return;
    }

    this.minimize = minimize;
    clearKhAutomatonCache();
  }

  @Override
  protected GraphDeterministicAutomaton<Integer, Action> buildKhAutomaton(Expression initExpr, Expression endExpr) {
    return AutomataOperations.intersection(cond1(initExpr), cond2(initExpr, endExpr));
  }

  private GraphDeterministicAutomaton<Integer, Action> cond1(Expression initExpr) {
    Set<GraphDeterministicAutomaton<Integer, Action>> automataSet = new HashSet<>();

    for (State state : statesHolding(initExpr)) {
      GraphDeterministicAutomaton<Integer, Action> aStar = aStar(state);

      if (minimize) {
        aStar = AutomataOperations.minimize(aStar);
      }

      automataSet.add(aStar);
    }

    if (automataSet.isEmpty()) {
      return GraphDeterministicAutomaton.empty();
    }
    return AutomataOperations.intersection(automataSet);
  }

  private GraphDeterministicAutomaton<Integer, Action> cond2(Expression initExpr, Expression endExpr) {
    Set<GraphDeterministicAutomaton<Integer, Action>> automatonSet = new HashSet<>();

    for (State initState : statesHolding(initExpr)) {
      for (State endState : statesHolding(endExpr.not())) {
        GraphDeterministicAutomaton<Integer, Action> aComplement = aComplement(initState, endState);

        if (minimize) {
          aComplement = AutomataOperations.minimize(aComplement);
        }

        automatonSet.add(aComplement);
      }
    }

    if (automatonSet.isEmpty()) {
      return GraphDeterministicAutomaton.empty();
    }
    return AutomataOperations.intersection(automatonSet);
  }

  private GraphDeterministicAutomaton<Integer, Action> aStar(State state) {
    Stack<Set<State>> stack = new Stack<>();
    Set<Set<State>> visited = new HashSet<>();
    Set<State> initialStateSet = new HashSet<>(Set.of(state));

    GraphDeterministicAutomaton<Set<State>, Action> automaton = new GraphDeterministicAutomaton<>();
    automaton.setInitialState(initialStateSet);

    stack.push(initialStateSet);

    while (!stack.isEmpty()) {
      Set<State> x = stack.pop();
      visited.add(x);

      for (Action action : lts.getActions()) {
        lts.targets(x, action, true).ifPresent(target -> {
          automaton.addTransition(x, target, action);

          if (!visited.contains(target))
            stack.push(target);
        });
      }
    }

    automaton.addFinalStates(automaton.getStates());

    return AutomataOperations.toIntegerStates(automaton);
  }

  private GraphDeterministicAutomaton<Integer, Action> aComplement(State initState, State endState) {
    GraphDeterministicAutomaton<State, Action> automaton = new GraphDeterministicAutomaton<>();

    for (State source : lts.getStates()) {
      for (Action action : lts.getActions()) {
        lts.targets(source, action).forEach(target -> automaton.addTransition(source, target, action));
      }
    }

    automaton.setInitialState(initState);
    automaton.addFinalState(endState);

    return AutomataOperations.toIntegerStates(AutomataOperations.complement(automaton));
  }
}
