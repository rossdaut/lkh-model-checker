package lkh.modelchecker.classic;

import lkh.lts.LTS;
import lkh.modelchecker.AutomataModelChecker;
import lkh.modelchecker.ClassicAutomataModelChecker;
import lkh.modelchecker.KhAutomataModelCheckerTest;

class KhClassicAutomataModelCheckerTest extends KhAutomataModelCheckerTest {
  @Override
  protected AutomataModelChecker<Integer, Character> createModelChecker(LTS<Integer, Character> lts) {
    return new ClassicAutomataModelChecker<>(lts, 0);
  }
}
