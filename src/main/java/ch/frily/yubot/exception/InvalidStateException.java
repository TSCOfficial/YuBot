package ch.frily.yubot.exception;

import org.slf4j.event.Level;

public class InvalidStateException extends ClientException {

    private static final String DEFAULT_MESSAGE = "Ungültiger Zustand!";

    public InvalidStateException(String hint) {
        super(DEFAULT_MESSAGE, hint);
        this.setLogLevel(Level.WARN);
    }

    public InvalidStateException(String message, String hint) {
        super(message, hint);
        this.setLogLevel(Level.WARN);
    }
}
