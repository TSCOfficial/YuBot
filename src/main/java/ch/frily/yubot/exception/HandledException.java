package ch.frily.yubot.exception;

/**
 * Internal marker exception thrown by {@link ExceptionHandler#fail(Throwable)}.
 * It indicates that the underlying problem has already been fully handled
 * (logged and/or reported to the user) and only serves to abort the current
 * control flow. It must not be handled again.
 */
public class HandledException extends RuntimeException {
    public HandledException() {
        super(null, null, false, false); // no message, no stack trace -> cheap
    }
}
