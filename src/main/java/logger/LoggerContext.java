package logger;

/**
 * Thread-local context for loggers.
 * Allows setting a logger that will be automatically used by graph structures
 * without needing to pass the logger explicitly through method parameters.
 *
 * Usage:
 * <pre>
 *   try (var scope = LoggerContext.withLogger(myLogger)) {
 *     // All graphs created here will use myLogger
 *     modelChecker.check(expression);
 *   }
 * </pre>
 */
public final class LoggerContext {
    private static final ThreadLocal<Logger> CURRENT_LOGGER = new ThreadLocal<>();

    private LoggerContext() {
    }

    /**
     * Set the logger.
     * @param logger the logger to use (can be null to disable logging)
     */
    public static void setLogger(Logger logger) {
        if (logger == null) {
            CURRENT_LOGGER.remove();
        } else {
            CURRENT_LOGGER.set(logger);
        }
    }

    /**
     * Set the logger for the current thread and return a scope that restores the previous value when closed.
     *
     * @param logger the logger to use inside the scope
     * @return an autocloseable scope that restores the previous logger
     */
    public static AutoCloseable withLogger(Logger logger) {
        Logger previousLogger = CURRENT_LOGGER.get();
        setLogger(logger);
        return () -> {
            if (previousLogger == null) {
                CURRENT_LOGGER.remove();
            } else {
                CURRENT_LOGGER.set(previousLogger);
            }
        };
    }

    /**
     * Get the current logger.
     * @return the current logger, or null if not set
     */
    public static Logger getLogger() {
        return CURRENT_LOGGER.get();
    }

    /**
     * Clear the logger.
     */
    public static void clearLogger() {
        CURRENT_LOGGER.remove();
    }

    /**
     * Check if a logger is currently set.
     * @return true if a logger is set
     */
    public static boolean hasLogger() {
        return CURRENT_LOGGER.get() != null;
    }
}
