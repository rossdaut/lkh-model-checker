package lkh.modelchecker.classic;

import lkh.lts.LTS;
import lkh.modelchecker.AbstractAutomataModelChecker;
import lkh.modelchecker.ClassicAutomataModelChecker;
import lkh.modelchecker.KhAutomataModelCheckerTest;

class ClassicKhAutomataModelCheckerTest extends KhAutomataModelCheckerTest {
  @Override
  protected AbstractAutomataModelChecker<Integer, Character> createModelChecker(LTS<Integer, Character> lts) {
    return new ClassicAutomataModelChecker<>(lts, 0);
  }
}
