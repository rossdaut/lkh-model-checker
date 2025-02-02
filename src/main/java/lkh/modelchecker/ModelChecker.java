package lkh.modelchecker;

import lkh.automata.AutomataIterator;
import lkh.automata.AutomataOperations;
import lkh.automata.DeterministicAutomaton;
import lkh.dot.DotWriter;
import lkh.expression.Expression;
import lkh.lts.LTS;
import lombok.NonNull;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.stream.Collectors;

public class ModelChecker<State, Action> {
  private final LTS<State, Action> lts;
  private final State pointedState;
  @Getter @Setter private boolean minimize;

  public ModelChecker(@NonNull LTS<State, Action> lts, @NonNull State pointedState, boolean minimize) {
    if (!lts.containsState(pointedState))
      throw new IllegalArgumentException("pointedState not in lts");

    this.lts = lts;
    this.pointedState = pointedState;
    this.minimize = minimize;
  }

  public ModelChecker(@NonNull LTS<State, Action> lts, @NonNull State pointedState) {
    this(lts, pointedState, false);
  }

  /**
   * Return whether the LTS satisfies the expression, following the KH logic rules.
   * - LTS satisfies 'or', 'and', 'not' and 'implies' expression iff the pointed states satisfies it
   * - LTS satisfies a kh expression iff there exists a witness plan for it (see witnesses())
   * @param expr a non null KH-Logic expression
   * @return true if the LTS satisfies the given expression, false otherwise
   */
  public boolean check(@NonNull Expression expr) {
    return check(expr, pointedState);
  }

  /**
   * Return the plans that witness kh(initExpr, endExpr) and have length of at most lengthLimit.
   * A plan witnesses a kh expression if it satisfies (1) and (2).
   * (1) It is strongly executable for all states satisfying initExpr
   * (2) Applying it to a state satisfying initExpr, results in a state satisfying endExpr
   * @param initExpression the expression that source states must satisfy
   * @param endExpression the expression that end states must satisfy
   * @param lengthLimit the maximum plan length
   * @return the list of witness plans for kh(initExpr, endExpr)
   */
  public Iterator<List<Action>> witnesses(Expression initExpression, Expression endExpression, int lengthLimit) {
    return new AutomataIterator<>(khAutomaton(initExpression, endExpression), lengthLimit);
  }

  /**
   * Return whether the LTS satisfies the expression over the given state.
   * @param expr a non null KH-Logic expression
   * @param state the state to check
   * @return true if the state satisfies the expression, false otherwise
   */
  public boolean check(@NonNull Expression expr, State state) {
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

  /**
   * Return whether the LTS satisfies kh(left, right).
   * @param left initial expression
   * @param right end expression
   * @return whether the LTS satisfies kh(left, right)
   */
  private boolean kh(Expression left, Expression right) {
    return !khAutomaton(left, right).isEmpty();
  }

  /**
   * Construct the KH automaton by first building the cond1 and cond2 automata and intersect them
   * @param initExpr initial expression
   * @param endExpr end expression
   * @return the KH automaton
   */
  private DeterministicAutomaton<Integer, Action> khAutomaton(Expression initExpr, Expression endExpr) {
    DeterministicAutomaton<Integer, Action> khAutomaton = AutomataOperations.intersection(cond1(initExpr), cond2(initExpr, endExpr));
    DotWriter.writeDFA(khAutomaton, "khAutomaton" + (minimize ? "_min" : "") + ".dot");
    return khAutomaton;
  }

  /**
   * Return an automaton describing the plans that satisfy (1)
   * (1) The plan is strongly executable for all states satisfying initExpr
   * @param initExpr the expression that all source plans must satisfy
   * @return an automaton describing all plans that are SE over all states satisfying initExpr
   */
  private DeterministicAutomaton<Integer, Action> cond1(Expression initExpr) {
    Set<DeterministicAutomaton<Integer, Action>> automataSet = new HashSet<>();

    for (State state : statesHolding(initExpr)) {
      DeterministicAutomaton<Integer, Action> aStar = aStar(state);

      if (minimize) {
        aStar = AutomataOperations.minimize(aStar);
      }

      automataSet.add(aStar);
    }

    // TODO: Consultar
    if (automataSet.isEmpty()) {
      return DeterministicAutomaton.empty();
    }
    return AutomataOperations.intersection(automataSet);
  }

  /**
   * Return an automaton describing all plans that satisfy (2)
   * (2) When plan is applied to a state where initExpr holds, it leads to a state where endExpr holds
   * @param initExpr the expression that source states must satisfy
   * @param endExpr the expression that target states must satisfy
   * @return an automaton describing all plans that satisfy (2)
   */
  private DeterministicAutomaton<Integer, Action> cond2(Expression initExpr, Expression endExpr) {
    Set<DeterministicAutomaton<Integer, Action>> automatonSet = new HashSet<>();

    for (State initState : statesHolding(initExpr)) {
      for (State endState : statesHolding(endExpr.not())) {
        DeterministicAutomaton<Integer, Action> aComplement = aComplement(initState, endState);

        if (minimize) {
          aComplement = AutomataOperations.minimize(aComplement);
        }

        automatonSet.add(aComplement);
      }
    }

    if (automatonSet.isEmpty()) {
      return DeterministicAutomaton.empty();
    }
    return AutomataOperations.intersection(automatonSet);
  }

  /**
   * Return an automaton describing all plans that are SE over the given state
   * @param state the source state
   * @return an automaton describing all plans that are SE over state
   */
  private DeterministicAutomaton<Integer, Action> aStar(State state) {
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

    return AutomataOperations.toIntegerStates(automaton);
  }

  /**
   * Return an automaton describing all plans that lead to endState when applied to initState
   * @param initState the source state
   * @param endState the target state
   * @return an automaton describing all plans that lead to endState when applied to initState
   */
    private DeterministicAutomaton<Integer, Action> aComplement(State initState, State endState) {
    DeterministicAutomaton<State, Action> automaton = new DeterministicAutomaton<>();

    for (State source : lts.getStates()) {
      for (Action action : lts.getActions()) {
        lts.targets(source, action).forEach(target -> automaton.addTransition(source, target, action));
      }
    }

    automaton.setInitialState(initState);
    automaton.addFinalState(endState);

    return AutomataOperations.toIntegerStates(AutomataOperations.complement(automaton));
  }

  /**
   * Return the states where the given expression holds
   * @param expression the expression to check
   * @return a set of states where expression holds
   */
  private Set<State> statesHolding(Expression expression) {
    // TODO: Think about nested KH
    return lts.getStates().stream().filter(state -> check(expression, state)).collect(Collectors.toSet());
  }
}
