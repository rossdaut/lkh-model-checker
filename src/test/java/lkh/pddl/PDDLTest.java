package lkh.pddl;

import java.util.List;
import lkh.lts.LTS;
import lkh.lts.builder.ActionSelectionStrategy;
import lkh.lts.builder.PDDL;
import lkh.planning.pddl4j.Pddl4jProblem;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.FileNotFoundException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PDDLTest {
  static LTS<Integer, String> lts;
  static PDDL pddl;
  @BeforeAll
  static void setUp() throws FileNotFoundException {
    String resourcesPath = "src/test/resources";
    String domainFilename = resourcesPath + "/pddl/domain.pddl";
    String problemFilename = resourcesPath + "/pddl/problem.pddl";
    pddl = new PDDL(domainFilename, problemFilename);
    lts = pddl.buildLTS();
  }

  @ParameterizedTest
  @CsvSource({"0, 'p(x), p(y)'", "1, 'p(x)'", "2, 'p(y)'"})
  public void testInitialState(int state, String labels) {
    String[] labelArray = labels.split(",\\s*");
    assertEquals(Set.of(labelArray), lts.getLabels(state));
  }

  @ParameterizedTest
  @CsvSource({"0, 1, 'a'", "0, 2, 'b'", "0, 0, 'c'", "0, 0, 'd'", "1, 1, 'a'", "1, 0, 'c'", "2, 2, 'b'", "2, 0, 'd'"})
  public void testTransitions(int from, int to, String label) {
    assertTrue(lts.targets(from, label).contains(to));
  }

  @Test
  public void testRejectConditionalEffects() {
    String resourcesPath = "src/test/resources/pddl";
    String domainFilename = resourcesPath + "/conditional-domain.pddl";
    String problemFilename = resourcesPath + "/conditional-problem.pddl";

    assertThrows(IllegalArgumentException.class, () -> new Pddl4jProblem(domainFilename, problemFilename));
  }

  @Test
  public void testSetActionSelectionStrategyInvalidatesCachedLts() throws FileNotFoundException {
    String resourcesPath = "src/test/resources";
    String domainFilename = resourcesPath + "/pddl/domain.pddl";
    String problemFilename = resourcesPath + "/pddl/problem.pddl";
    PDDL builder = new PDDL(domainFilename, problemFilename);

    LTS<Integer, String> initialLts = builder.buildLTS();

    ActionSelectionStrategy noActionsStrategy = (previousAction, state, problem) -> List.of();

    builder.setActionSelectionStrategy(noActionsStrategy);
    LTS<Integer, String> rebuiltLts = builder.buildLTS();

    assertNotSame(initialLts, rebuiltLts);
    assertEquals(Set.of(0), rebuiltLts.getStates());
    assertEquals(Set.of(), rebuiltLts.getActions());
  }
}
