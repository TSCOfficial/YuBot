package ch.frily.yubot.storage;

import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * A key-value store that holds exactly one value type
 *
 * @param </T> the type of the stored values
 */
public class Storage<T> {

    @Getter
    @Setter
    private T value;

    public Storage(T value) {
        this.value = value;
    }
}
