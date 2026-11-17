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
     * @param column the database column to map the setters to
     * @return the entity field @Setter method
     */
    @SuppressWarnings("unchecked")
    public <T> Consumer<T> mapColumnToField(Table.Column column) {
        return switch (column) {
            case Table.SpecialInterestColumn.ID   -> (Consumer<T>) (Consumer<Integer>) this::setId;
            case Table.SpecialInterestColumn.NAME -> (Consumer<T>) (Consumer<String>) this::setName;
            case Table.SpecialInterestColumn.DESCRIPTION -> (Consumer<T>) (Consumer<String>) this::setDescription;
            case Table.SpecialInterestColumn.STATUS -> (Consumer<T>) (Consumer<RequestStatus>) this::setRequestStatus;
            default -> throw new IllegalStateException(
                    String.format("Could not map column '%s' to entity field", column.getColumn())
            );
        };
    }


}
