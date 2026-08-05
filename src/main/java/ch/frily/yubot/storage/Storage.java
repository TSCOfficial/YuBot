package ch.frily.yubot.storage;

import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * A key-value store that holds exactly one value type
 *
 * @param <V> the type of the stored values
 */
public abstract class Storage<V> {

    @Getter
    @Setter
    private V value;

    public StringStorage asString(){
        return (StringStorage) this;
    }

    public LongStorage asLong(){
        return (LongStorage) this;
    }

    public StringListStorage asStringList(){
        return (StringListStorage) this;
    }

    public BooleanStorage asBoolean(){
        return (BooleanStorage) this;
    }
}
