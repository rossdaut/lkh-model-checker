package lkh.generator.impl;

import lkh.lts.HashMapLTS;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GenerationContext {
  public final HashMapLTS<Integer, String> lts = new HashMapLTS<>();
  public final List<Set<String>> initialLabelPool = new ArrayList<>();
  public final List<Set<String>> goalLabelPool = new ArrayList<>();
  public final List<Set<String>> neutralLabelPool = new ArrayList<>();
  public final Set<Integer> initialStates = new LinkedHashSet<>();
  public final Set<Integer> goalStates = new LinkedHashSet<>();
  public final Map<Integer, Set<String>> protectedActionsByState = new LinkedHashMap<>();
  public int nextStateId = 0;
}
