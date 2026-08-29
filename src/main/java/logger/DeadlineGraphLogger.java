package logger;

/**
 * GraphLogger that aborts on a deadline. Throws {@link TimeoutSignal} once the
 * wall-clock time exceeds the configured deadline. Optionally runs a {@link Runnable}
 * every so many events (to snapshot partial progress to disk, useful when this logger
 * is used from a subprocess that may be terminated externally).
 *
 * Typical usage:
 *
 * <pre>
 *   long deadline = System.currentTimeMillis() + TIMEOUT_MS;
 *   DeadlineGraphLogger lg = new DeadlineGraphLogger("KH", deadline);
 *   try (var scope = LoggerContext.withLogger(lg)) {
 *     checker.check(query);
 *   } catch (DeadlineGraphLogger.TimeoutSignal e) {
 *     // partial stats available in lg.getGeneratedNodesCant() etc.
 *   }
 * </pre>
 */
public class DeadlineGraphLogger extends GraphLogger {
  // (t & MASK) == 0 is true once every MASK+1 events. We use it to run slow work
  // (like reading the clock) only sometimes, not on every event.
  private static final int CLOCK_MASK = 0xFF;     // check the clock every 256 events
  private static final int SNAPSHOT_MASK = 0x3FF; // snapshot every 1024 events

  private final long deadline;
  private final Runnable snapshot;
  private int tick;

  /** Constructor without snapshot (in-process use). */
  public DeadlineGraphLogger(String name, long deadline) {
    this(name, deadline, null);
  }

  /** Constructor with optional snapshot (subprocess use). */
  public DeadlineGraphLogger(String name, long deadline, Runnable snapshot) {
    super(name, false);
    this.deadline = deadline;
    this.snapshot = (snapshot != null) ? snapshot : () -> {};
  }

  @Override
  public void log(LogEvent event) {
    super.log(event);
    int t = ++tick;
    if ((t & SNAPSHOT_MASK) == 0) snapshot.run();
    if ((t & CLOCK_MASK) == 0 && System.currentTimeMillis() > deadline) {
      throw new TimeoutSignal();
    }
  }

  /** Thrown to abort processing once the deadline is exceeded. */
  public static final class TimeoutSignal extends RuntimeException {
    // Last arg disables stack-trace capture: this is control flow on a hot path,
    // thrown after potentially millions of events, and the trace is never read.
    public TimeoutSignal() { super(null, null, true, false); }
  }
}
