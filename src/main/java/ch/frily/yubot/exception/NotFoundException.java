package ch.frily.yubot.exception;

import org.slf4j.event.Level;

public class NotFoundException extends ClientException {
    private static final String DEFAULT_MESSAGE = "Objekt nicht gefunden.";

    public NotFoundException(String hint) {
        super(DEFAULT_MESSAGE, hint);
        this.setLogLevel(Level.ERROR);
    }

    public NotFoundException(String message, String hint) {
        super(message, hint);
        this.setLogLevel(Level.ERROR);
    }
}
