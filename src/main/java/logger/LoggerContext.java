package logger;

/**
 * Singleton context for loggers.
 * Allows setting a logger that will be automatically used by graph structures
 * without needing to pass the logger explicitly through method parameters.
 *
 * Usage:
 * <pre>
 *   LoggerContext.setLogger(myLogger);
 *   try {
 *       // All graphs created here will use myLogger
 *       modelChecker.check(expression);
 *   } finally {
 *       LoggerContext.clearLogger();
 *   }
 * </pre>
 */
public class LoggerContext {
    private static LoggerContext instance;
    private Logger currentLogger;

    private LoggerContext() {}

    private static LoggerContext getInstance() {
        if (instance == null) {
            instance = new LoggerContext();
        }
        return instance;
    }

    /**
     * Set the logger.
     * @param logger the logger to use (can be null to disable logging)
     */
    public static void setLogger(Logger logger) {
        getInstance().currentLogger = logger;
    }

    /**
     * Get the current logger.
     * @return the current logger, or null if not set
     */
    public static Logger getLogger() {
        return getInstance().currentLogger;
    }

    /**
     * Clear the logger.
     */
    public static void clearLogger() {
        getInstance().currentLogger = null;
    }

    /**
     * Check if a logger is currently set.
     * @return true if a logger is set
     */
    public static boolean hasLogger() {
        return getInstance().currentLogger != null;
    }
}

