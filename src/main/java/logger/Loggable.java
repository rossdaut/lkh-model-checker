package logger;

public interface Loggable {
  Logger getLogger();

  void registerLogger(Logger logger);

  void log(String event);
}
