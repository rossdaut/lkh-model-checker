package lkh.modelchecker.direct;

import lkh.lts.LTS;
import lkh.modelchecker.AutomataModelChecker;
import lkh.modelchecker.DirectAutomataModelChecker;
import lkh.modelchecker.KhAutomataModelCheckerTest;

class KhDirectAutomataModelCheckerTest extends KhAutomataModelCheckerTest {
  @Override
  protected AutomataModelChecker<Integer, Character> createModelChecker(LTS<Integer, Character> lts) {
    return new DirectAutomataModelChecker<>(lts, 0);
  }
}
