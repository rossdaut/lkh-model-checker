package lkh.pddl;

import lkh.lts.LTS;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.FileNotFoundException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PDDLTest {
  static LTS<Integer, String> lts;
  @BeforeAll
  static void setUp() throws FileNotFoundException {
    String resourcesPath = "src/test/resources";
    String domainFilename = resourcesPath + "/pddl/domain.pddl";
    String problemFilename = resourcesPath + "/pddl/problem.pddl";
    lts = PDDL.asLTS(domainFilename, problemFilename);
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
}
