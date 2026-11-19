package ch.frily.yubot.database;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <h1>Entity mapper</h1>
 * This maps entities from a {@link ResultSet} to their corresponding Java object.
 *
 * @since 2.5.0
 * @author Aliz Rose Frily
 */
@Slf4j
public final class EntityMapper {

    /** Caches fields to prevent scans at each request */
    private static final Map<Class<?>, Map<String, Field>> FIELD_CACHE = new ConcurrentHashMap<>();

    private EntityMapper() {}

    /**
     * Builds the given java objects (defined by type) by mapping all columns, present in the {@link ResultSet}, to the objects themselves.
     * @param rs The {@link ResultSet} to map
     * @param type The type of the Java objects to build
     * @return The built Java objects
     * @param <T> The type of the Java objects to build
     * @throws SQLException If an error occurs while mapping the {@link ResultSet}
     */
    public static <T> @NonNull List<T> build(@NonNull ResultSet rs, Class<T> type) throws SQLException {
        Map<String, Field> fields = FIELD_CACHE.computeIfAbsent(type, EntityMapper::scanFields);
        ResultSetMetaData meta = rs.getMetaData();
        int columnCount = meta.getColumnCount();

        List<T> entities = new ArrayList<>();
        while (rs.next()) {
            T entity = newInstance(type);

            for (int i = 1; i <= columnCount; i++) {
                Field field = fields.get(normalize(meta.getColumnLabel(i)));
                // handle "_id"-columns
                if (field == null && meta.getColumnLabel(i).endsWith("_id")) {
                    String fieldname = normalize(meta.getColumnLabel(i).substring(0, meta.getColumnLabel(i).length() - 3));
                    field = fields.get(fieldname);
                }
                try {
                    if (field == null) {
                        log.warn("Field {} not found in class {}. Ignoring database column.", normalize(meta.getColumnLabel(i)), type.getSimpleName());
                        continue; // ignore missing field
                    }

                    Object value;
                    try {
                        value = readValue(rs, i, field);
                        log.info("reading value for {}", field.getName());
                    } catch (SQLException e) {
                        // use custom mappers
                        log.info("executing custom mapper for {}", field.getName());
                        value = type.getMethod("customMapping", Field.class, Object.class).invoke(entity, field, rs.getObject(i));
                    }

                    field.set(entity, value);
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException("Cannot set field " + field.getName(), e);
                } catch (NoSuchMethodException | InvocationTargetException e) {
                    throw new IllegalStateException("Cannot find customMapping method for field " + field.getName(), e);
                }
            }
            entities.add(entity);
        }
        return entities;
    }


    /**
     * Read a value from the ResultSet and convert it to the appropriate type
     * <p>
     *     If the value is part of an enum, the enum constant is directly called<br>
     * </p>
     * @param rs the ResultSet to read from
     * @param index the index of the column to read
     * @param field the field itself
     * @return the value read from the ResultSet
     * @throws SQLException type mismatch - the field uses a custom object that is neither a primitive (or their wrapper) nor an enum
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static @Nullable Object readValue(ResultSet rs, int index, @NonNull Field field) throws SQLException {
        if (field.getType().isEnum()) {
            String name = rs.getString(index);
            return name == null ? null : Enum.valueOf((Class<? extends Enum>) field.getType(), name);
        }
        return rs.getObject(index, box(field.getType()));

    }

    /**
     * Wrap primitive types into their corresponding wrapper classes
     * @param type
     * @return
     */
    private static Class<?> box(@NonNull Class<?> type) {
        if (!type.isPrimitive()) return type;
        if (type == int.class) return Integer.class;
        if (type == long.class) return Long.class;
        if (type == boolean.class) return Boolean.class;
        if (type == double.class) return Double.class;
        if (type == float.class) return Float.class;
        if (type == short.class) return Short.class;
        if (type == byte.class) return Byte.class;
        return type;
    }

    /**
     * Scan class fields
     * @param type the class type to scan
     * @return a map of the fieldnames with their appropriate Fields
     */
    private static Map<String, Field> scanFields(@NonNull Class<?> type) {
        Map<String, Field> map = new HashMap<>();
        for (Field field : type.getDeclaredFields()) {
            if (field.isSynthetic() || java.lang.reflect.Modifier.isStatic(field.getModifiers())) continue;
            field.setAccessible(true);
            map.put(normalize(field.getName()), field);
        }
        return map;
    }

    /**
     * Normalize column/field names
     * <p>
     *     <pre><code>"column_name" and "columnName" -> "columnname"</code></pre>
     *     This allows to map columns to fields and vice versa, without having to worry about case sensitivity or underscores.
     * </p>
     * @param name
     * @return a normalized column/field name
     */
    private static @NonNull String normalize(@NonNull String name) {
        return name.replace("_", "").toLowerCase();
    }

    /**
     * Create new instance of the given class type
     * <p>
     *     Validates instance creation for the given class type
     * </p>
     * @param type The class type to create a new instance of
     * @return a new instance of the given type
     * @param <T> The type of the class to create a new instance of
     */
    private static <T> @NonNull T newInstance(@NonNull Class<T> type) {
        try {
            var ctor = type.getDeclaredConstructor();
            ctor.setAccessible(true);
            return ctor.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(type.getSimpleName() + " needs a no-args constructor", e);
        }
    }
}
