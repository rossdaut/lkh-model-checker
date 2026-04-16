package lkh.pddl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import lkh.dot.DotReader;
import lkh.dot.DotWriter;
import lkh.lts.builder.ActionSelectionStrategy;
import lkh.lts.builder.DefaultActionSelectionStrategy;
import lkh.lts.builder.PDDL;
import lkh.por.StrongStubbornSetActionSelectionStrategy;
import lkh.por.StratifiedActionSelectionStrategy;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class PDDLActionSelectionStrategiesTest {
  private static final String RESOURCES_PATH = "src/test/resources/pddl/strategies";
  private static final String DOMAIN = RESOURCES_PATH + "/tire-domain.pddl";
  private static final String PROBLEM = RESOURCES_PATH + "/tire-problem.pddl";

  @ParameterizedTest
  @MethodSource("strategies")
  public void testStrategyMatchesExpectedLts(ActionSelectionStrategy strategy, String expectedDot) throws Exception {
    PDDL builder = new PDDL(DOMAIN, PROBLEM, strategy);
    Path tempFile = Files.createTempFile("lts_strategy_", ".dot");

    try {
      DotWriter.writeLTS(builder.buildLTS(), tempFile.toString());
      assertEquals(DotReader.readLTS(expectedDot), DotReader.readLTS(tempFile.toString()));
    } finally {
      Files.deleteIfExists(tempFile);
    }
  }

  private static Stream<Arguments> strategies() {
    return Stream.of(
        Arguments.of(new DefaultActionSelectionStrategy(), RESOURCES_PATH + "/expected_none.dot"),
        Arguments.of(new StratifiedActionSelectionStrategy(), RESOURCES_PATH + "/expected_stratified.dot"),
        Arguments.of(new StrongStubbornSetActionSelectionStrategy(), RESOURCES_PATH + "/expected_sss.dot")
    );
  }
}
