package ch.frily.yubot.service.storage;

import lombok.Getter;
import lombok.Setter;

/**
 * A key-value store that holds exactly one value type
 *
 * @param <T> the type of the stored values
 */
public class Storage<T> {

    @Getter
    @Setter
    private T value;

    public Storage(T value) {
        this.value = value;
    }
}
