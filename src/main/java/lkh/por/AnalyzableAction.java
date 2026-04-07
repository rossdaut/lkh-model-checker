package lkh.por;

import java.util.Collection;
import lkh.planning.Action;
import lkh.planning.Fluent;

public interface AnalyzableAction extends Action {
  Collection<Fluent> getDependentFluents();

  Collection<Fluent> getAffectedFluents();

  Collection<Fluent> getTransitionFluents();
}
