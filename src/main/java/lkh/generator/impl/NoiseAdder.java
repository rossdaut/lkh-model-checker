package lkh.generator.impl;

import lkh.generator.LtsGeneratorConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class NoiseAdder {
  private static final int MAX_ATTEMPTS = 1_000;

  private final LtsGeneratorConfig config;
  private final Random random;

  public NoiseAdder(LtsGeneratorConfig config, Random random) {
    this.config = config;
    this.random = random;
  }

  public void add(GenerationContext ctx) {
    while (ctx.lts.getSize().key() < config.minNodeCount()) {
      createNoiseState(ctx);
    }
    while (ctx.lts.getSize().value() < config.minEdgeCount()) {
      if (!tryAddNoiseEdge(ctx)) {
        createNoiseState(ctx);
      }
    }
  }

  private void createNoiseState(GenerationContext ctx) {
    int state = ctx.nextStateId++;
    Set<String> labels = removeRandom(ctx.neutralLabelPool);
    ctx.lts.addState(state, labels);
    attachNoiseState(ctx, state);
  }

  private void attachNoiseState(GenerationContext ctx, int noiseState) {
    if (ctx.lts.getStates().size() <= 1) return;

    for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
      int anchor = randomState(ctx);
      if (anchor == noiseState) continue;

      if (random.nextBoolean() && tryAddNoiseEdge(ctx, anchor, noiseState)) return;
      if (tryAddNoiseEdge(ctx, noiseState, anchor)) return;
      if (tryAddNoiseEdge(ctx, anchor, noiseState)) return;
    }
  }

  private boolean tryAddNoiseEdge(GenerationContext ctx) {
    if (ctx.lts.getStates().isEmpty()) return false;

    for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
      if (tryAddNoiseEdge(ctx, randomState(ctx), randomState(ctx))) return true;
    }

    return false;
  }

  private boolean tryAddNoiseEdge(GenerationContext ctx, int source, int target) {
    if (source == target && ctx.lts.getStates().size() > 1 && random.nextBoolean()) {
      target = randomState(ctx);
    }

    List<String> available = availableNoiseActions(ctx, source);
    if (available.isEmpty()) return false;

    String action = available.get(random.nextInt(available.size()));
    if (ctx.lts.targets(source, action).contains(target)) return false;

    ctx.lts.addTransition(source, target, action);
    return true;
  }

  private List<String> availableNoiseActions(GenerationContext ctx, int source) {
    List<String> available = new ArrayList<>(config.actions());
    Set<String> protected_ = ctx.protectedActionsByState.getOrDefault(source, Set.of());
    available.removeIf(protected_::contains);

    if (config.deterministic()) {
      available.removeIf(action -> !ctx.lts.targets(source, action).isEmpty());
    }

    return available;
  }

  private int randomState(GenerationContext ctx) {
    List<Integer> states = new ArrayList<>(ctx.lts.getStates());
    return states.get(random.nextInt(states.size()));
  }

  private Set<String> removeRandom(List<Set<String>> pool) {
    if (pool.isEmpty()) {
      throw new IllegalArgumentException("Not enough unique labels in pool");
    }
    return pool.remove(random.nextInt(pool.size()));
  }
}
