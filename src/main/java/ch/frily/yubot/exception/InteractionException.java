package ch.frily.yubot.exception;

import lombok.Getter;
import lombok.Setter;
import org.slf4j.event.Level;

/**
 * Exception that can be reported back to the user during a Discord interaction.
 * <p>
 * In addition to a user-facing message, an optional {@code hint} can be provided.
 * The hint is rendered small (using Discord's {@code -#} subtext markdown) below
 * the error message and is meant to explain <i>why</i> the error happened
 * (e.g. missing permissions).
 */
@Getter
public class InteractionException extends ClientException {

    /** Small subtext shown below the error message. May be <code>null</code>. */
    private final String hint;

    /** Whether the reply to the user should be ephemeral. */
    private final boolean ephemeral;

    public InteractionException(String message) {
        this(message, null);
    }

    public InteractionException(String message, String hint) {
        this(message, hint, true, null, Level.ERROR, false);
    }

    public InteractionException(String message, String hint, boolean ephemeral) {
        this(message, hint, ephemeral, null, Level.ERROR, false);
    }

    public InteractionException(String message,
                                String hint,
                                boolean ephemeral,
                                Throwable cause,
                                Level logLevel,
                                boolean logStackTrace) {
        super(message, cause, logLevel, logStackTrace);
        this.hint = hint;
        this.ephemeral = ephemeral;
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
