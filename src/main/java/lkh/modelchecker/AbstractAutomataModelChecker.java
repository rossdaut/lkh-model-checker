package lkh.modelchecker;

import lkh.automata.impl.AutomataIterator;
import lkh.automata.impl.GraphDeterministicAutomaton;
import lkh.expression.Expression;
import lkh.lts.LTS;
import lkh.utils.Pair;
import logger.Logger;
import logger.LoggerContext;
import lombok.NonNull;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class AbstractAutomataModelChecker<State, Action> implements ModelChecker<State, Action> {
  protected final LTS<State, Action> lts;
  protected final State pointedState;
  private final Map<Expression, GraphDeterministicAutomaton<Integer, Action>> khAutomatonCache = new HashMap<>();

  protected AbstractAutomataModelChecker(@NonNull LTS<State, Action> lts, @NonNull State pointedState) {
    if (!lts.containsState(pointedState))
      throw new IllegalArgumentException("pointedState not in lts");

    this.lts = lts;
    this.pointedState = pointedState;
  }

  /**
   * Return whether the LTS satisfies the expression, following the KH logic rules.
   * - LTS satisfies 'or', 'and', 'not' and 'implies' expression iff the pointed states satisfies it
   * - LTS satisfies a kh expression iff there exists a witness plan for it (see witnesses())
   * @param expr a non null KH-Logic expression
   * @return true if the LTS satisfies the given expression, false otherwise
   */
  @Override
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
  @Override
  public Iterator<List<Action>> witnesses(Expression initExpression, Expression endExpression, int lengthLimit) {
    return new AutomataIterator<>(khAutomaton(initExpression, endExpression), lengthLimit);
  }

  /**
   * Return whether the LTS satisfies the expression over the given state.
   * @param expr a non null KH-Logic expression
   * @param state the state to check
   * @return true if the state satisfies the expression, false otherwise
   */
  @Override
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

  private boolean kh(Expression left, Expression right) {
    return !khAutomaton(left, right).isEmpty();
  }

  protected final GraphDeterministicAutomaton<Integer, Action> khAutomaton(Expression initExpr, Expression endExpr) {
    Expression key = Expression.kh(initExpr, endExpr);
    GraphDeterministicAutomaton<Integer, Action> automaton = khAutomatonCache.computeIfAbsent(
        key,
        k -> buildKhAutomaton(initExpr, endExpr)
    );
    logAutomatonSize(automaton);
    return automaton;
  }

  protected abstract GraphDeterministicAutomaton<Integer, Action> buildKhAutomaton(Expression initExpr, Expression endExpr);

  protected final void clearKhAutomatonCache() {
    khAutomatonCache.clear();
  }

  private void logAutomatonSize(GraphDeterministicAutomaton<Integer, Action> automaton) {
    Logger logger = LoggerContext.getLogger();
    if (logger != null) {
      Pair<Integer, Integer> size = automaton.getSize();
      logger.setSize(size);
    }
  }

  /**
   * Return the states where the given expression holds
   * @param expression the expression to check
   * @return a set of states where expression holds
   */
  protected final Set<State> statesHolding(Expression expression) {
    // TODO: Think about nested KH
    return lts.getStates().stream().filter(state -> check(expression, state)).collect(Collectors.toSet());
  }
}
