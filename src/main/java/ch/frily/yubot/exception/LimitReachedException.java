package ch.frily.yubot.exception;

import org.slf4j.event.Level;

public class LimitReachedException extends ClientException {
    private static final String DEFAULT_MESSAGE = "Limite erreicht!";

    public LimitReachedException(String hint) {
        super(DEFAULT_MESSAGE, hint);
        this.setLogLevel(Level.WARN);
    }

    public LimitReachedException(String message, String hint) {
        super(message, hint);
        this.setLogLevel(Level.WARN);
    }
}
