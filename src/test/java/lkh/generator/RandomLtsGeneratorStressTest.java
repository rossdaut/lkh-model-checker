package lkh.generator;

import lkh.expression.Expression;
import lkh.expression.parser.ParseException;
import lkh.modelchecker.DirectAutomataModelChecker;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("stress")
class RandomLtsGeneratorStressTest {
  private static final int CASE_COUNT = 10_000;
  private static final long MASTER_SEED = 20260417L;

  @ParameterizedTest
  @MethodSource("randomConfigs")
  @Timeout(5)
  void generatedWitnessesAreAcceptedByModelChecker(LtsGeneratorConfig config) {
    GeneratedLts generated = new RandomLtsGenerator(config).generate();
    DirectAutomataModelChecker<Integer, String> modelChecker =
        new DirectAutomataModelChecker<>(generated.lts(), generated.initialStates().iterator().next());

    Set<List<String>> actualWitnesses = new HashSet<>();
    modelChecker
        .witnesses(config.initialCondition(), config.goalCondition(), config.minWitnessActionCount())
        .forEachRemaining(actualWitnesses::add);

    assertTrue(modelChecker.check(Expression.kh(config.initialCondition(), config.goalCondition())));
    assertTrue(actualWitnesses.containsAll(generated.implantedWitnesses()));
    assertEquals(config.initialStateCount(), generated.initialStates().size());
  }

  private static Stream<LtsGeneratorConfig> randomConfigs() {
    List<LtsGeneratorConfig> configs = new ArrayList<>();
    for (int i = 0; i < CASE_COUNT; i++) {
      configs.add(randomConfig(i));
    }
    return configs.stream();
  }

  private static LtsGeneratorConfig randomConfig(int index) {
    Random random = new Random(MASTER_SEED + index);

    int propositionCount = 8 + random.nextInt(2);
    int actionCount = 2 + random.nextInt(5);
    int witnessLength = 1 + random.nextInt(5);
    int witnessCapacity = (int) Math.pow(actionCount, witnessLength);
    int witnessCount = 1 + random.nextInt(Math.min(6, witnessCapacity));
    int initialStateCount = 1 + random.nextInt(3);
    int goalStateCount = 1 + random.nextInt(5);
    int minNodeCount = 6 + random.nextInt(14);
    int minEdgeCount = minNodeCount + random.nextInt(actionCount * 2 + 1);
    long seed = random.nextLong();

    int initialProp = random.nextInt(propositionCount);
    int goalProp = (initialProp + 1 + random.nextInt(propositionCount - 1)) % propositionCount;
    StringBuilder goalCondition = new StringBuilder("p").append(goalProp).append(" and not p").append(initialProp);

    if (propositionCount > 2 && random.nextBoolean()) {
      int forbiddenProp;
      do {
        forbiddenProp = random.nextInt(propositionCount);
      } while (forbiddenProp == goalProp || forbiddenProp == initialProp);

      goalCondition.append(" and not p").append(forbiddenProp);
    }

    return new LtsGeneratorConfig(
        random.nextBoolean(),
        minNodeCount,
        minEdgeCount,
        actionCount,
        propositionCount,
        expression("p" + initialProp),
        expression(goalCondition.toString()),
        initialStateCount,
        goalStateCount,
        witnessCount,
        witnessLength,
        seed
    );
  }

  private static Expression expression(String value) {
    try {
      return Expression.of(value);
    } catch (ParseException e) {
      throw new IllegalArgumentException(e);
    }
  }
}
