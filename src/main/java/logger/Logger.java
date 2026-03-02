package logger;

import lkh.utils.Pair;

public interface Logger {
  void log(String event);
  void printLog();
  void setSize(Pair<Integer, Integer> size);
}
