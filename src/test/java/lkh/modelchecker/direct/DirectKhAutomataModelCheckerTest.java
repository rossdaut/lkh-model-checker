package lkh.modelchecker.direct;

import lkh.lts.LTS;
import lkh.modelchecker.AbstractAutomataModelChecker;
import lkh.modelchecker.AutomataModelChecker;
import lkh.modelchecker.KhAutomataModelCheckerTest;

class DirectKhAutomataModelCheckerTest extends KhAutomataModelCheckerTest {
  @Override
  protected AbstractAutomataModelChecker<Integer, Character> createModelChecker(LTS<Integer, Character> lts) {
    return new AutomataModelChecker<>(lts, 0);
  }
}
