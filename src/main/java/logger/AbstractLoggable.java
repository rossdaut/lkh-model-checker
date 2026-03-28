package logger;

public class AbstractLoggable implements Loggable {
  private Logger logger;

  @Override
  public Logger getLogger() {
    return logger;
  }

  @Override
  public void registerLogger(Logger logger) {
    this.logger = logger;
  }

  @Override
  public void log(LogEvent event) {
    if (logger != null) {
      logger.log(event);
    }
  }
}
