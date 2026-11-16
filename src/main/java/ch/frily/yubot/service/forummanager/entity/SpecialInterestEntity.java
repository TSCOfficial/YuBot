package ch.frily.yubot.service.forummanager.entity;

import ch.frily.yubot.database.Table;
import ch.frily.yubot.exception.InvalidStateException;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.function.Consumer;

/**
 * Interest Entity
 * <p>
 *     Saves all data for a special interest
 * </p>
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
     * Build the entity
     */
    public class Builder {

        /**
         * Get the field setter from the appropriate {@link ch.frily.yubot.database.Table.Column Table.Column}
         * @return
         */

    }

    /**
     * Maps the entity fields, defined in this class, to the database columns from {@link Table.Column}
     * @param column the database column to
     * @return
     * @param <T>
     */
    public Consumer<Integer> mapDbColumnToEntity(Table.Column column) {
        switch (column) {
            case Table.SpecialInterestColumn.ID -> {
                return this::setId;

            }
            default -> throw new InvalidStateException(String.format("Could not map Table '%s' to entity field", column.getColumn()));
        }
    }
}
