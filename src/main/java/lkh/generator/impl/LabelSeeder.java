package lkh.generator.impl;

import lkh.expression.Expression;
import lkh.generator.LtsGeneratorConfig;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class LabelSeeder {
  private final LtsGeneratorConfig config;
  private final Random random;

  public LabelSeeder(LtsGeneratorConfig config, Random random) {
    this.config = config;
    this.random = random;
  }

  public void seed(GenerationContext ctx) {
    buildLabelPools(ctx);
    createInitialStates(ctx);
    createGoalStates(ctx);
  }

  private void buildLabelPools(GenerationContext ctx) {
    collectLabels(ctx, config.propositions(), 0, new LinkedHashSet<>());
  }

  private void collectLabels(GenerationContext ctx, List<String> propositions, int index, Set<String> labels) {
    if (index == propositions.size()) {
      classifyLabels(ctx, Set.copyOf(labels));
      return;
    }
    collectLabels(ctx, propositions, index + 1, labels);
    labels.add(propositions.get(index));
    collectLabels(ctx, propositions, index + 1, labels);
    labels.remove(propositions.get(index));
  }

  private void classifyLabels(GenerationContext ctx, Set<String> labels) {
    if (holds(config.initialCondition(), labels)) {
      ctx.initialLabelPool.add(labels);
    } else if (holds(config.goalCondition(), labels)) {
      ctx.goalLabelPool.add(labels);
    } else {
      ctx.neutralLabelPool.add(labels);
    }
  }

  private void createInitialStates(GenerationContext ctx) {
    for (int i = 0; i < config.initialStateCount(); i++) {
      registerState(ctx, removeRandom(ctx.initialLabelPool));
    }
  }

  private void createGoalStates(GenerationContext ctx) {
    while (ctx.goalStates.size() < config.goalStateCount()) {
      registerState(ctx, removeRandom(ctx.goalLabelPool));
    }
  }

  private void registerState(GenerationContext ctx, Set<String> labels) {
    int state = ctx.nextStateId++;
    ctx.lts.addState(state, labels);
    if (holds(config.initialCondition(), labels)) ctx.initialStates.add(state);
    if (holds(config.goalCondition(), labels)) ctx.goalStates.add(state);
  }

  private Set<String> removeRandom(List<Set<String>> pool) {
    if (pool.isEmpty()) {
      throw new IllegalArgumentException("Not enough unique labels in pool");
    }
    return pool.remove(random.nextInt(pool.size()));
  }

  static boolean holds(Expression expression, Set<String> labels) {
    return switch (expression.getTokenType()) {
      case PROP -> labels.contains(expression.getName());
      case NOT -> !holds(expression.getRight(), labels);
      case AND -> holds(expression.getLeft(), labels) && holds(expression.getRight(), labels);
      case OR -> holds(expression.getLeft(), labels) || holds(expression.getRight(), labels);
      case IMPLIES -> !holds(expression.getLeft(), labels) || holds(expression.getRight(), labels);
      case KH -> throw new IllegalArgumentException("KH expressions are not supported for generation conditions");
    };
  }
}
