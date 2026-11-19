package es.ull.simulation.hta.osdi;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.Property;

/**
 * Captures the messages logged by the classes of JaDES while it is open, so that the tests can check the warnings of the generators.
 * Usage: {@code try (LogCapture log = LogCapture.start()) { ...; assertTrue(log.contains("...")); }}
 */
public final class LogCapture implements AutoCloseable {
    /** The logger whose configuration receives the messages of JaDES (see log4j2-test.xml) */
    private static final String LOGGER_NAME = "es.ull.simulation";
    private static final String APPENDER_NAME = "LogCapture";

    private final List<String> messages = new CopyOnWriteArrayList<>();
    private final LoggerContext context;
    private final LoggerConfig config;
    private final AbstractAppender appender;

    private LogCapture() {
        context = (LoggerContext) LogManager.getContext(false);
        final Configuration configuration = context.getConfiguration();
        config = configuration.getLoggerConfig(LOGGER_NAME);
        appender = new AbstractAppender(APPENDER_NAME, null, null, true, Property.EMPTY_ARRAY) {
            @Override
            public void append(LogEvent event) {
                messages.add(event.getLevel() + " " + event.getMessage().getFormattedMessage());
            }
        };
        appender.start();
        config.addAppender(appender, Level.ALL, null);
        context.updateLoggers();
    }

    /**
     * Starts capturing the messages
     * @return The capture, which must be closed
     */
    public static LogCapture start() {
        return new LogCapture();
    }

    /**
     * Returns the captured messages, each one preceded by its level
     * @return The captured messages
     */
    public List<String> getMessages() {
        return messages;
    }

    /**
     * Returns true if a warning containing a text was captured
     * @param text The text
     * @return true if a warning containing the text was captured
     */
    public boolean containsWarning(String text) {
        return messages.stream().anyMatch(message -> message.startsWith("WARN ") && message.contains(text));
    }

    @Override
    public void close() {
        config.removeAppender(APPENDER_NAME);
        appender.stop();
        context.updateLoggers();
    }
}
