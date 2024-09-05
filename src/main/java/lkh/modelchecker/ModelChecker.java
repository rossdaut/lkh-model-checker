package lkh.modelchecker;

import lkh.automata.AutomataIterator;
import lkh.automata.AutomataOperations;
import lkh.automata.DeterministicAutomaton;
import lkh.expression.Expression;
import lkh.lts.LTS;
import lombok.NonNull;

import java.util.*;
import java.util.stream.Collectors;

public class ModelChecker<State, Action> {
  private LTS<State, Action> lts;
  private State pointedState;

  public ModelChecker(@NonNull LTS<State, Action> lts, @NonNull State pointedState) {
    if (!lts.containsState(pointedState))
      throw new IllegalArgumentException("pointedState not in lts");

    this.lts = lts;
    this.pointedState = pointedState;
  }

  public boolean check(@NonNull Expression expr) {
    return check(expr, pointedState);
  }

  public Iterator<List<Action>> witnesses(Expression initExpression, Expression endExpression, int lengthLimit) {
    return new AutomataIterator<>(khAutomaton(initExpression, endExpression), lengthLimit);
  }

  private boolean check(@NonNull Expression expr, State state) {
    Expression left = expr.getLeft();
    Expression right = expr.getRight();

    return switch (expr.getTokenType()) {
      case KH -> kh(left, right);
      case IMPLIES -> !check(left, state) || check(right, state);
      case OR -> check(left, state) || check(right, state);
      case AND -> check(left, state) && check(right, state);
      case NOT -> !check(right, state);
      case PROP -> lts.getLabels(state).contains(expr.getName());
    };
  }

  private boolean kh(Expression left, Expression right) {
    return !khAutomaton(left, right).isEmpty();
  }

  private DeterministicAutomaton<Integer, Action> khAutomaton(Expression initExpr, Expression endExpr) {
    return AutomataOperations.intersection(cond1(initExpr), cond2(initExpr, endExpr));
  }

  private DeterministicAutomaton<Integer, Action> cond1(Expression initExpr) {
    Set<DeterministicAutomaton<Set<State>, Action>> automataSet = new HashSet<>();

    for (State state : statesHolding(initExpr)) {
      automataSet.add(aStar(state));
    }

    // TODO: Consultar
    if (automataSet.isEmpty()) {
      return DeterministicAutomaton.empty();
    }
    return AutomataOperations.intersection(automataSet);
  }

  private DeterministicAutomaton<Integer, Action> cond2(Expression initExpr, Expression endExpr) {
    Set<DeterministicAutomaton<State, Action>> automatonSet = new HashSet<>();

    for (State initState : statesHolding(initExpr)) {
      for (State endState : statesHolding(endExpr.not())) {
        automatonSet.add(aComplement(initState, endState));
      }
    }

    if (automatonSet.isEmpty()) {
      return DeterministicAutomaton.empty();
    }
    return AutomataOperations.intersection(automatonSet);
  }

  private DeterministicAutomaton<Set<State>, Action> aStar(State state) {
    Stack<Set<State>> stack = new Stack<>();
    Set<Set<State>> visited = new HashSet<>();
    Set<State> initialStateSet = new HashSet<>(Set.of(state));

    DeterministicAutomaton<Set<State>, Action> automaton = new DeterministicAutomaton<>();
    automaton.setInitialState(initialStateSet);

    stack.push(initialStateSet);

    while (!stack.isEmpty()) {
      Set<State> X = stack.pop();
      visited.add(X);

      for (Action a : lts.getActions()) {
        lts.targets(X, a, true).ifPresent(Y -> {
          automaton.addTransition(X, Y, a);

          if (!visited.contains(Y))
            stack.push(Y);
        });
      }
    }

    automaton.addFinalStates(automaton.getStates());

    return automaton;
  }

  private DeterministicAutomaton<State, Action> aComplement(State initState, State endState) {
    DeterministicAutomaton<State, Action> automaton = new DeterministicAutomaton<>();

    for (State source : lts.getStates()) {
      for (Action action : lts.getActions()) {
        lts.targets(source, action).forEach(target -> {
          automaton.addTransition(source, target, action);
        });
      }
    }

    automaton.setInitialState(initState);
    automaton.addFinalState(endState);

    return AutomataOperations.complement(automaton);
  }

  private Set<State> statesHolding(Expression expression) {
    // TODO: Think about nested KH
    return lts.getStates().stream().filter(state -> check(expression, state)).collect(Collectors.toSet());
  }
}
