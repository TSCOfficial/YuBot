package ch.frily.yubot.exception;

import org.slf4j.event.Level;

/**
 * Thrown when a user tries to perform an action they are not allowed to.
 * The hint explains who is allowed to perform the action.
 */
public class PermissionDeniedException extends ClientException {

    private static final String DEFAULT_MESSAGE = "Du bist nicht dazu berechtigt!";

    /**
     *
     * @param hint "Nur xy kann z/diese Aktion ausführen."
     */
    public PermissionDeniedException(String hint) {
        super(DEFAULT_MESSAGE, hint);
        this.setLogStackTrace(false);
    }

    /**
     * @param message Custom message instead of the default one.
     * @param hint "Nur xy kann z/diese Aktion ausführen."
     */
    public PermissionDeniedException(String message, String hint) {
        super(message, hint);
    }
}
