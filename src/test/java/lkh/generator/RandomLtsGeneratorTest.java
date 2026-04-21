package lkh.generator;

import lkh.expression.Expression;
import lkh.expression.parser.ParseException;
import lkh.modelchecker.DirectAutomataModelChecker;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RandomLtsGeneratorTest {
  @ParameterizedTest
  @MethodSource("deterministicConfigs")
  void deterministicGenerationProducesWitnessesAcceptedByModelChecker(LtsGeneratorConfig config) {
    GeneratedLts generated = new RandomLtsGenerator(config).generate();

    assertWitness(config, generated);
  }

  @ParameterizedTest
  @MethodSource("nondeterministicConfigs")
  void nondeterministicGenerationProducesWitnessesAcceptedByModelChecker(LtsGeneratorConfig config) {
    GeneratedLts generated = new RandomLtsGenerator(config).generate();

    assertWitness(config, generated);
    assertTrue(hasNondeterministicBranch(generated));
  }

  @ParameterizedTest
  @MethodSource("mergedIntermediateConfigs")
  void witnessesCanReuseExistingIntermediateNodes(LtsGeneratorConfig config) {
    GeneratedLts generated = new RandomLtsGenerator(config).generate();

    assertWitness(config, generated);
    assertTrue(hasMergedIntermediateState(generated));
  }

  private void assertWitness(LtsGeneratorConfig config, GeneratedLts generated) {
    DirectAutomataModelChecker<Integer, String> modelChecker =
        new DirectAutomataModelChecker<>(generated.lts(), generated.initialStates().iterator().next());

    Set<List<String>> actualWitnesses = new HashSet<>();
    modelChecker
        .witnesses(config.initialCondition(), config.goalCondition(), config.minWitnessActionCount())
        .forEachRemaining(actualWitnesses::add);

    assertTrue(modelChecker.check(Expression.kh(config.initialCondition(), config.goalCondition())));
    assertTrue(actualWitnesses.containsAll(generated.implantedWitnesses()));
    assertEquals(config.initialStateCount(), generated.initialStates().size());
    assertTrue(generated.goalStates().size() >= config.goalStateCount());
  }

  private static Stream<LtsGeneratorConfig> deterministicConfigs() {
    return Stream.of(
        new LtsGeneratorConfig(true, 6, 4, 2, 4, expression("p0"), expression("p1 and not p0"), 1, 1, 1, 1, 1L),
        new LtsGeneratorConfig(true, 10, 12, 3, 5, expression("p0"), expression("p1 and not p0"), 1, 2, 2, 2, 2L),
        new LtsGeneratorConfig(true, 18, 24, 4, 8, expression("p0"), expression("p1 and not p0"), 2, 2, 3, 3, 7L),
        new LtsGeneratorConfig(true, 20, 35, 5, 8, expression("p2"), expression("p3 and not p2"), 3, 2, 4, 2, 19L),
        new LtsGeneratorConfig(true, 28, 45, 6, 9, expression("p1"), expression("p4 and not p1 and not p2"), 2, 4, 5, 3, 31L),
        new LtsGeneratorConfig(true, 14, 20, 2, 8, expression("p0 or p1"), expression("p2 and not p0 and not p1"), 2, 2, 2, 3, 43L),
        new LtsGeneratorConfig(true, 16, 25, 3, 7, expression("p3 and not p4"), expression("p4 and not p3"), 2, 2, 3, 2, 59L),
        new LtsGeneratorConfig(true, 24, 28, 4, 8, expression("not p0"), expression("p0 and p1"), 3, 3, 2, 3, 61L),
        new LtsGeneratorConfig(true, 12, 18, 4, 6, expression("p2"), expression("p0 and not p2"), 1, 3, 6, 2, 73L),
        new LtsGeneratorConfig(true, 30, 60, 5, 10, expression("p5"), expression("p6 and not p5"), 3, 3, 4, 4, 101L)
    );
  }

  private static Stream<LtsGeneratorConfig> nondeterministicConfigs() {
    return Stream.of(
        new LtsGeneratorConfig(false, 8, 25, 2, 6, expression("p0"), expression("p1 and not p0"), 1, 1, 1, 1, 13L),
        new LtsGeneratorConfig(false, 10, 28, 2, 5, expression("p0"), expression("p1 and not p0"), 1, 2, 2, 2, 17L),
        new LtsGeneratorConfig(false, 12, 35, 3, 6, expression("p2"), expression("p3 and not p2"), 2, 2, 2, 2, 23L),
        new LtsGeneratorConfig(false, 14, 40, 3, 7, expression("p1"), expression("p4 and not p1"), 2, 3, 3, 2, 29L),
        new LtsGeneratorConfig(false, 16, 45, 3, 8, expression("p0 or p2"), expression("p3 and not p0 and not p2"), 2, 2, 2, 3, 37L),
        new LtsGeneratorConfig(false, 18, 60, 4, 8, expression("p3 and not p4"), expression("p4 and not p3"), 2, 2, 3, 3, 41L),
        new LtsGeneratorConfig(false, 20, 70, 4, 9, expression("not p0"), expression("p0 and p1"), 3, 3, 2, 3, 53L),
        new LtsGeneratorConfig(false, 22, 85, 4, 9, expression("p5"), expression("p6 and not p5 and not p7"), 2, 3, 4, 2, 67L),
        new LtsGeneratorConfig(false, 24, 95, 5, 10, expression("p2"), expression("p8 and not p2"), 3, 3, 3, 3, 83L),
        new LtsGeneratorConfig(false, 26, 110, 5, 10, expression("p1 and not p9"), expression("p9 and not p1"), 2, 4, 4, 3, 97L)
    );
  }

  private static Stream<LtsGeneratorConfig> mergedIntermediateConfigs() {
    return Stream.of(
        new LtsGeneratorConfig(true, 3, 4, 2, 2, expression("p0"), expression("p1 and not p0"), 1, 1, 3, 2, 1L),
        new LtsGeneratorConfig(true, 3, 4, 2, 2, expression("p0"), expression("p1 and not p0"), 1, 1, 3, 2, 2L),
        new LtsGeneratorConfig(true, 3, 4, 2, 2, expression("p0"), expression("p1 and not p0"), 1, 1, 3, 2, 3L),
        new LtsGeneratorConfig(true, 3, 4, 2, 2, expression("p0"), expression("p1 and not p0"), 1, 1, 3, 2, 4L),
        new LtsGeneratorConfig(true, 3, 4, 2, 2, expression("p0"), expression("p1 and not p0"), 1, 1, 4, 2, 0L),
        new LtsGeneratorConfig(true, 3, 4, 2, 2, expression("p0"), expression("p1 and not p0"), 1, 1, 4, 2, 1L),
        new LtsGeneratorConfig(true, 3, 4, 2, 2, expression("p0"), expression("p1 and not p0"), 1, 1, 4, 2, 2L),
        new LtsGeneratorConfig(true, 3, 5, 3, 2, expression("p0"), expression("p1 and not p0"), 1, 1, 4, 2, 0L),
        new LtsGeneratorConfig(true, 3, 5, 3, 2, expression("p0"), expression("p1 and not p0"), 1, 1, 4, 2, 1L),
        new LtsGeneratorConfig(true, 3, 5, 3, 2, expression("p0"), expression("p1 and not p0"), 1, 1, 4, 2, 2L)
    );
  }

  private static Expression expression(String value) {
    try {
      return Expression.of(value);
    } catch (ParseException e) {
      throw new IllegalArgumentException(e);
    }
  }

  private boolean hasNondeterministicBranch(GeneratedLts generated) {
    for (Integer state : generated.lts().getStates()) {
      for (String action : generated.lts().getActions(state)) {
        if (generated.lts().targets(state, action).size() > 1) {
          return true;
        }
      }
    }

    return false;
  }

  private boolean hasMergedIntermediateState(GeneratedLts generated) {
    for (Integer state : generated.lts().getStates()) {
      if (generated.initialStates().contains(state) || generated.goalStates().contains(state)) {
        continue;
      }
      if (incomingEdgeCount(generated, state) > 1) {
        return true;
      }
    }

    return false;
  }

  private int incomingEdgeCount(GeneratedLts generated, Integer target) {
    int count = 0;

    for (Integer source : generated.lts().getStates()) {
      for (String action : generated.lts().getActions(source)) {
        if (generated.lts().targets(source, action).contains(target)) {
          count++;
        }
      }
    }

    return count;
  }
}
