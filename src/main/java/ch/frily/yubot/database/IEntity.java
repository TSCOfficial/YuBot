package ch.frily.yubot.database;

import java.lang.reflect.Field;

public interface IEntity {

    default Object customMapping(Field field, Object value) {
        return null;
    };
}
