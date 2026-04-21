package lkh.generator;

import lkh.expression.Expression;

import java.util.Objects;

public record LtsGeneratorConfig(
    boolean deterministic,
    int minNodeCount,
    int minEdgeCount,
    int actionCount,
    int propositionCount,
    Expression initialCondition,
    Expression goalCondition,
    int initialStateCount,
    int goalStateCount,
    int witnessCount,
    int minWitnessActionCount,
    long seed
) {
  public LtsGeneratorConfig {
    Objects.requireNonNull(initialCondition, "initialCondition");
    Objects.requireNonNull(goalCondition, "goalCondition");

    if (minNodeCount <= 0) {
      throw new IllegalArgumentException("minNodeCount must be positive");
    }
    if (minEdgeCount < 0) {
      throw new IllegalArgumentException("minEdgeCount must be non-negative");
    }
    if (actionCount <= 0) {
      throw new IllegalArgumentException("actionCount must be positive");
    }
    if (propositionCount < 0) {
      throw new IllegalArgumentException("propositionCount must be non-negative");
    }
    if (initialStateCount <= 0) {
      throw new IllegalArgumentException("initialStateCount must be positive");
    }
    if (goalStateCount <= 0) {
      throw new IllegalArgumentException("goalStateCount must be positive");
    }
    if (witnessCount <= 0) {
      throw new IllegalArgumentException("witnessCount must be positive");
    }
    if (minWitnessActionCount <= 0) {
      throw new IllegalArgumentException("minWitnessActionCount must be positive");
    }

    validateExpressionVocabulary(initialCondition, propositionCount);
    validateExpressionVocabulary(goalCondition, propositionCount);
    validateWitnessCapacity(actionCount, minWitnessActionCount, witnessCount);
  }

  private static void validateExpressionVocabulary(Expression expression, int propositionCount) {
    for (String proposition : expression.propositionNames()) {
      if (!proposition.startsWith("p") || proposition.length() == 1) {
        throw new IllegalArgumentException(
            "condition uses proposition \"" + proposition + "\" outside the generated vocabulary"
        );
      }

      int index = Integer.parseInt(proposition.substring(1));
      if (index < 0 || index >= propositionCount) {
        throw new IllegalArgumentException(
            "condition uses proposition \"" + proposition + "\" outside the generated vocabulary"
        );
      }
    }
  }

  private static void validateWitnessCapacity(int actionCount, int witnessLength, int witnessCount) {
    if (Math.pow(actionCount, witnessLength) < witnessCount) {
      throw new IllegalArgumentException(
          "Not enough distinct witness plans can be generated with the requested actionCount "
              + "and minWitnessActionCount"
      );
    }
  }
}
