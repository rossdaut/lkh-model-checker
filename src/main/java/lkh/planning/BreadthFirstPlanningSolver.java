package lkh.planning;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

public final class BreadthFirstPlanningSolver {
  private BreadthFirstPlanningSolver() {
  }

  public static SearchResult solve(Problem problem, int maxVisitedStates) {
    State initialState = problem.getInitialState();
    if (satisfies(initialState, problem.getGoalCondition())) {
      return new SearchResult(true, 1, false);
    }

    ArrayDeque<State> frontier = new ArrayDeque<>();
    Set<State> visited = new HashSet<>();
    frontier.add(initialState);
    visited.add(initialState);

    while (!frontier.isEmpty()) {
      State state = frontier.removeFirst();

      for (Action action : problem.getApplicableActions(state)) {
        State next = state.copy();
        next.apply(action);

        if (!visited.add(next)) {
          continue;
        }
        if (satisfies(next, problem.getGoalCondition())) {
          return new SearchResult(true, visited.size(), false);
        }
        if (visited.size() >= maxVisitedStates) {
          return new SearchResult(false, visited.size(), true);
        }
        frontier.addLast(next);
      }
    }

    return new SearchResult(false, visited.size(), false);
  }

  private static boolean satisfies(State state, Condition condition) {
    return condition.getLiterals().stream().allMatch(state::holds);
  }

  public record SearchResult(boolean solved, int visitedStates, boolean hitLimit) {
  }
}
