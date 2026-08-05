package ch.frily.yubot.storage;

/**
 * Stores flags
 */
public class BooleanStorage extends Storage<Boolean> {

    public BooleanStorage(boolean value) {
        this.setValue(value);
    }
}
