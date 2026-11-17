package ch.frily.yubot.service.forummanager.entity;

import ch.frily.yubot.database.Table;
import ch.frily.yubot.exception.InvalidStateException;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.lang.constant.Constable;
import java.util.function.Consumer;

/**
 * Interest Entity
 * <p>
 *     Saves all data for a special interest
 * </p>
 * @since v2.5.2
 * @author Aliz Rose Frily
 */
public class SpecialInterestEntity {

    @Getter
    @Setter
    private int id;

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private String description;

    @Getter
    @Setter
    private RequestStatus requestStatus;

    public enum RequestStatus {
        REQUESTED,
        APPROVED,
        DENIED
    }

    /**
     * Maps the entity fields, defined in this class, to the database columns from {@link Table.Column}
     * <p>
     *     <b>Primitive types</b><br>
     *     Primitive types can be mapped directly. Example:
     *     <pre><code>
     *          return (Consumer<T>) switch (column) {
     *             case Table.SpecialInterestColumn.ID   -> (Consumer<Integer>) this::setId; // returns the setter directly
     *             ...
     *     </code></pre>
     * </p>
     * <p>
     *     <b>Objects</b>
     *     Because the database only stores primitive types, any Object (such as {@link Enum Enums} or custom Objects) need to be mapped additionaly. Example:
     *     <pre><code>
     *         case Table.SpecialInterestColumn.STATUS -> (Consumer<String>) enumKey -> { // the column saves "REQUESTED"
     *                 RequestStatus statusObject = RequestStatus.valueOf(enumKey); // get enum constant wih name "REQUESTED"
     *                 setRequestStatus(statusObject); // set enum constant
     *             };
     *     </code></pre>
     *     This returns the lambda in which the value is mapped AND set
     * </p>
     * @param column the database column to map the setters to
     * @return the entity field @Setter method
     */
    @SuppressWarnings("unchecked")
    public <T> Consumer<T> mapColumnToField(Table.Column column) {
        return (Consumer<T>) switch (column) {
            case Table.SpecialInterestColumn.ID   -> (Consumer<Integer>) this::setId;
            case Table.SpecialInterestColumn.NAME -> (Consumer<String>) this::setName;
            case Table.SpecialInterestColumn.DESCRIPTION -> (Consumer<String>) this::setDescription;
            case Table.SpecialInterestColumn.STATUS -> (Consumer<String>) enumKey -> {
                RequestStatus statusObject = RequestStatus.valueOf(enumKey);
                setRequestStatus(statusObject);
            };
            default -> throw new IllegalStateException(
                    String.format("Could not map column '%s' to entity field", column.getColumn())
            );
        };
    }


}
