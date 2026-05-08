package lkh.generator;

import lkh.generator.impl.GenerationContext;
import lkh.generator.impl.LabelSeeder;
import lkh.generator.impl.NoiseAdder;
import lkh.generator.impl.WitnessImplanter;

import java.util.List;
import java.util.Random;
import java.util.Set;

public class RandomLtsGenerator {
  private final LtsGeneratorConfig config;
  private final Random random;

  public RandomLtsGenerator(LtsGeneratorConfig config) {
    if (config == null) {
      throw new IllegalArgumentException("config must not be null");
    }
    this.config = config;
    this.random = new Random(config.seed());
  }

  public GeneratedLts generate() {
    GenerationContext ctx = new GenerationContext();
    new LabelSeeder(config, random).seed(ctx);
    List<List<String>> witnesses = new WitnessImplanter(config, random).implant(ctx);
    new NoiseAdder(config, random).add(ctx);
    return new GeneratedLts(
        config,
        ctx.lts,
        witnesses.stream().map(List::copyOf).toList(),
        Set.copyOf(ctx.initialStates),
        Set.copyOf(ctx.goalStates)
    );
  }
}
