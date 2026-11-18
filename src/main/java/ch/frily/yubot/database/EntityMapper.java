package ch.frily.yubot.database;

import lombok.extern.slf4j.Slf4j;

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
     * Map all columns, present in the {@link ResultSet}, to the corresponding Java objects.
     * @param rs The {@link ResultSet} to map
     * @param type The type of the Java objects to map to
     * @return The mapped Java objects
     * @param <T> The type of the Java objects to map to
     * @throws SQLException If an error occurs while mapping the {@link ResultSet}
     */
    public static <T> List<T> mapAll(ResultSet rs, Class<T> type) throws SQLException {
        Map<String, Field> fields = FIELD_CACHE.computeIfAbsent(type, EntityMapper::scanFields);
        ResultSetMetaData meta = rs.getMetaData();
        int columnCount = meta.getColumnCount();

        List<T> entities = new ArrayList<>();
        while (rs.next()) {
            T entity = newInstance(type);

            for (int i = 1; i <= columnCount; i++) {
                Field field = fields.get(normalize(meta.getColumnLabel(i)));
                try {
                    if (field == null) {
                        continue; // ignore missing field
                    }

                    log.info("Reading value from column {} for field {} with type {}", normalize(meta.getColumnLabel(i)), field.getName(), field.getType().getSimpleName());
                    Object value;
                    try {
                        value = readValue(rs, i, field);
                    } catch (SQLException e) {
                        log.info("could not map value for field {}, using customMapping", field.getName());
                        value = type.getMethod("customMapping", Field.class, Object.class).invoke(entity, field, rs.getObject(i));
                    }

                    log.info("Value: {}", value);
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
     *     If the value is part of a enum, the enum constant is directly called<br>
     *     If the value is a custom Object, the customMapping method is called<br>
     * </p>
     * @param rs the ResultSet to read from
     * @param index the index of the column to read
     * @param field the field itself
     * @return the value read from the ResultSet
     * @throws SQLException if an error occurs while reading from the ResultSet
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Object readValue(ResultSet rs, int index, Field field) throws SQLException {
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
    private static Class<?> box(Class<?> type) {
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
    private static Map<String, Field> scanFields(Class<?> type) {
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
     *     "column_name" and "columnName" -> "columnname"
     * </p>
     * @param name
     * @return
     */
    private static String normalize(String name) {
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
    private static <T> T newInstance(Class<T> type) {
        try {
            var ctor = type.getDeclaredConstructor();
            ctor.setAccessible(true);
            return ctor.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(type.getSimpleName() + " needs a no-args constructor", e);
        }
    }
}
