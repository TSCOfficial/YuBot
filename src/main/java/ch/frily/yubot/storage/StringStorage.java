package ch.frily.yubot.storage;

/**
 * Stores plain text values
 */
public class StringStorage extends Storage<String> {

    public StringStorage(String value) {
        this.setValue(value);
    }
}
