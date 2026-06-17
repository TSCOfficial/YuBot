package ch.frily.yubot.exception;

import lombok.Getter;
import lombok.Setter;
import org.slf4j.event.Level;

/**
 * Base class for every custom exception of the client.

 * Extend this class for exceptions that should only be logged.
 * Extend {@link InteractionException} for exceptions whose message should
 * also be reported back to the user on Discord.
 */
@Getter
public class ClientException extends RuntimeException {

    /** Level at which this exception will be logged. */
    private Level logLevel;

    /** Whether the full stack trace should be logged. */
    private final boolean logStackTrace;

    public ClientException(String message) {
        this(message, null, Level.ERROR, true);
    }

    public ClientException(String message, Throwable cause) {
        this(message, cause, Level.ERROR, true);
    }

    public ClientException(String message, Throwable cause, Level logLevel, boolean logStackTrace) {
        super(message, cause);
        this.logLevel = logLevel;
        this.logStackTrace = logStackTrace;
    }

    protected void setLogLevel(Level logLevel) {
        this.logLevel = logLevel;
    }
}
