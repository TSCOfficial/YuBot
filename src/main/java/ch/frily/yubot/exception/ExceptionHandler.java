package ch.frily.yubot.exception;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import org.slf4j.event.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Central place for handling exceptions throughout the client.
 * <p></p>
 * Usage:
 * <pre>{@code
 * try {
 *     // ... handle inter-/action
 * } catch (Exception e) {
 *     ExceptionHandler.handle(e, event | null);
 * }
 * }</pre>
 *
 * Behaviour:
 * <ul>
 *     <li>Every exception is logged according to its configured {@link Level}.</li>
 *     <li>Exceptions that are handled with an event are additionally reported back to the user with an optional hint.</li>
 *     <li>Unknown exceptions are wrapped and logged as errors, the user gets a generic fallback message.</li>
 * </ul>
 */
@Slf4j
@UtilityClass
public final class ExceptionHandler {

    private static final String GENERIC_USER_MESSAGE =
            "Es ist ein unerwarteter Fehler aufgetreten.";
    private static final String GENERIC_USER_HINT =
            "Bitte versuche es später erneut oder kontaktiere das Team.";

    /**
     * Handle an exception that occurred outside an interaction (log only).
     *
     * @param throwable the thrown exception
     */
    public static void handle(Throwable throwable) {
        handle(throwable, null);
    }

    /**
     * Handle an exception that occurred during an interaction.
     *
     * @param throwable the thrown exception
     * @param callback  the interaction callback (may be <code>null</code> for non-interaction context)
     */
    public static void handle(Throwable throwable, @Nullable IReplyCallback callback) {
        Throwable cause = unwrap(throwable);

        // ignore already handled exception
        if (cause instanceof HandledException) {
            return;
        }

        if (cause instanceof ClientException clientException) {
            log(clientException);

            if (callback != null) {
                replyToUser(callback, clientException.toUserMessage());
            }
            return;
        }

        // Unknown / unexpected exception
        log.error("Unhandled exception: {}", cause.getMessage(), cause);
        if (callback != null) {
            String errorMessage = "";
            if (cause.getMessage() != null) {
                errorMessage = "```\n" + cause.getMessage() + "\n```";
            }
            replyToUser(callback, String.format("%s\n%s\n-# %s", GENERIC_USER_MESSAGE, errorMessage, GENERIC_USER_HINT));
        }
    }

    /**
     * Same as {@link #handle(Throwable, IReplyCallback)}, but it throws an {@link HandledException}.
     * Use it as a {@code return}-replacement in methods that must return a value other than void:
     * <pre>{@code
     * return ExceptionHandler.fail(exception);
     * }</pre>
     *
     * @return nothing; the declared type only satisfies the compiler by throwing an internal exception
     */
    public static <T> T fail(Throwable throwable) {
        return fail(throwable, null);
    }

    /**
     * Same as {@link #handle(Throwable, IReplyCallback)}, but it throws an {@link HandledException}.
     * Use it as a {@code return}-replacement in methods that must return a value other than void:
     * <pre>{@code
     * return ExceptionHandler.fail(exception, event);
     * }</pre>
     *
     * @return nothing; the declared type only satisfies the compiler by throwing an internal exception
     */
    public static <T> T fail(Throwable throwable, @Nullable IReplyCallback callback) {
        handle(throwable, callback);
        throw new HandledException();
    }


    /**
     * Logs a {@link ClientException} using its configured level and stack-trace flag.
     */
    private static void log(ClientException exception) {
        Level level = exception.getLogLevel();
        String message = exception.getMessage();
        Throwable cause = exception.isLogStackTrace() ? exception : exception.getCause();

        switch (level) {
            case ERROR -> log.error(message, cause);
            case WARN -> log.warn(message, cause);
            case INFO -> log.info(message, cause);
            case DEBUG -> log.debug(message, cause);
            case TRACE -> log.trace(message, cause);
        }

    }

    /**
     * Sends a message back to the user if possible.
     * Handles both un-acknowledged and already acknowledged interactions.
     */
    private static void replyToUser(IReplyCallback callback, String message) {
        if (callback == null) {
            return;
        }
        try {
            if (callback.isAcknowledged()) {
                callback.getHook()
                        .sendMessage(message)
                        .setAllowedMentions(List.of())
                        .setEphemeral(true)
                        .queue();
            } else {
                callback.reply(message)
                        .setAllowedMentions(List.of())
                        .setEphemeral(true)
                        .queue();
            }
        } catch (Exception sendError) {
            log.error("Failed to send error message to user: {}", sendError.getMessage(), sendError);
        }
    }

    /**
     * Unwraps known, empty exception wrappers (defined in {@link #isTransparentWrapper(Throwable)}
     * <p>
     * Stops as soon as a {@link ClientException} or any non-wrapper exception
     * is reached (prevents generic errors and destroying custom exception handling).
     */
    private static Throwable unwrap(Throwable throwable) {
        if (throwable == null) {
            return new ClientException("Unknown error (null throwable)");
        }

        Throwable current = throwable;
        while (isTransparentWrapper(current) && current.getCause() != null) {
            current = current.getCause();
        }
        return current;
    }

    /**
     * Whether the given throwable is a "transparent" wrapper that carries no
     * own exception-data.
     * @return True if it's a wrapper, false if it's an exception class with its own exception-data
     */
    private static boolean isTransparentWrapper(Throwable throwable) {
        if (throwable instanceof ClientException) {
            return false;
        }
        return throwable instanceof java.util.concurrent.CompletionException
                || throwable instanceof java.util.concurrent.ExecutionException;
    }
}
