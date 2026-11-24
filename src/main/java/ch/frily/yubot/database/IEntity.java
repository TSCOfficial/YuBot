package ch.frily.yubot.database;

import java.lang.reflect.Field;

/**
 * Interface for database entities.
 * @since v2.5.0
 * @author Aliz Rose Frily
 */
public interface IEntity {

    /**
     * Map fields with custom objects
     * <p>
     *     Using the field, the correct mapper is determined and its defined action is executed, which returns an instance of the object specified as the field type.<br>
     *
     *     <b>Example</b><br>
     *     You have a java object "Topic" with a field "category" of type Category:
     *     <pre><code>
     *     // Topic.java
     *     public Object customMapping(Field field, Object value) {
     *         switch (field.getName()) {
     *             case "category" -> {
     *                 Category category = new Category();
     *                 category.setName((String) value);
     *                 return category;
     *             }
     *             ...
     *     }
     *     </code></pre>
     * </p>
     * @param field the field to map
     * @param value the raw value to map into the field
     * @return an instance of the object specified as the field type
     */
    default Object customMapping(Field field, Object value) {
        return null;
    };
}
