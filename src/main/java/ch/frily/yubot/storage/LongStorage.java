package ch.frily.yubot.storage;

/**
 * Stores numbers, most notably Discord snowflake ids
 */
public class LongStorage extends Storage<Long> {

    public LongStorage(long value) {
        this.setValue(value);
    }
}
