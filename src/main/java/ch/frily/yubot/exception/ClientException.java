package ch.frily.yubot.exception;

import lombok.Getter;
import lombok.Setter;
import org.slf4j.event.Level;

/**
 * Base class for every custom exception of the client.

 * Extend this class for exceptions that should be more specific (such as {@link PermissionDeniedException}).
 * If the exception is handled together with an event (see {@link ExceptionHandler}), then the user will be informed.
 */
@Getter
public class ClientException extends RuntimeException {

    /** Level at which this exception will be logged. */
    private Level logLevel;

    /** Whether the full stack trace should be logged. */
    private final boolean logStackTrace;

    /** Small subtext shown below the error message. May be <code>null</code>. */
    private final String hint;

    public ClientException(String message) {
        this(message, null, null, Level.ERROR, true);
    }

    public ClientException(String message, String hint) {
        this(message, hint, null, Level.ERROR, true);
    }

    public ClientException(String message, String hint, Throwable cause) {
        this(message, hint, cause, Level.ERROR, true);
    }

    public ClientException(String message, String hint, Throwable cause, Level logLevel, boolean logStackTrace) {
        super(message, cause);
        this.hint = hint;
        this.logLevel = logLevel;
        this.logStackTrace = logStackTrace;
    }

    protected void setLogLevel(Level logLevel) {
        this.logLevel = logLevel;
    }

    /**
     * Builds the message that is sent back to the user on Discord.
     * The optional hint is appended as small subtext.
     *
     * @return formatted user response message
     */
    public String toUserMessage() {
        String message = setMessageIcon(this.getLogLevel());
        if (hint == null || hint.isBlank()) {
            return message;
        }
        return message + "\n-# " + hint;
    }

    /**
     * Set the icon for the message based on the log level.
     * @param level
     * @return formatted log-level-based message
     */
    private String setMessageIcon(Level level) {
        String icon = switch (level) {
            case ERROR -> "❌";
            case WARN -> "⚠️";
            case INFO -> "ℹ️";
            default -> null;
        };
        if (icon != null) {
            return icon + " " + getMessage();
        }
        return getMessage();
    }
}
