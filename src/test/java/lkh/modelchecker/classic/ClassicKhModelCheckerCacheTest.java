package lkh.modelchecker.classic;

import lkh.expression.Expression;
import lkh.expression.parser.ParseException;
import lkh.modelchecker.AbstractAutomataModelChecker;
import lkh.modelchecker.ClassicAutomataModelChecker;
import lkh.modelchecker.KhAutomataModelCheckerTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClassicKhModelCheckerCacheTest {
  private ClassicAutomataModelChecker<Integer, Character> modelChecker;

  @BeforeEach
  void setUp() {
    modelChecker = new ClassicAutomataModelChecker<>(KhAutomataModelCheckerTest.buildLts(), 0);
  }

  @Test
  void setMinimizeClearsCacheWhenValueChanges() throws ParseException, ReflectiveOperationException {
    modelChecker.check(Expression.of("kh(p, p)"));
    assertEquals(1, khAutomatonCache().size());

    modelChecker.setMinimize(true);

    assertEquals(0, khAutomatonCache().size());
  }

  @Test
  void setMinimizeKeepsCacheWhenValueDoesNotChange() throws ParseException, ReflectiveOperationException {
    modelChecker.check(Expression.of("kh(p, p)"));
    assertEquals(1, khAutomatonCache().size());

    modelChecker.setMinimize(false);

    assertEquals(1, khAutomatonCache().size());
  }

  @SuppressWarnings("unchecked")
  private Map<Expression, ?> khAutomatonCache() throws ReflectiveOperationException {
    Field field = AbstractAutomataModelChecker.class.getDeclaredField("khAutomatonCache");
    field.setAccessible(true);
    return (Map<Expression, ?>) field.get(modelChecker);
  }
}
