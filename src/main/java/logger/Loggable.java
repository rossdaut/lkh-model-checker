package logger;

public interface Loggable {
  Logger getLogger();

  void registerLogger(Logger ILogger);

  void log(String event);
}
