package ch.frily.yubot.service.forummanager.entity;

import ch.frily.yubot.database.IEntity;
import ch.frily.yubot.database.Table;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.function.Consumer;

/**
 * Interest Entity
 * <p>
 *     Saves all data for a special interest
 * </p>
 * @since v2.5.2
 * @author Aliz Rose Frily
 */
public class SpecialInterestEntity implements IEntity {

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

    @Getter
    @Setter
    private InterestGroup interestGroup;

    public enum RequestStatus {
        REQUESTED,
        APPROVED,
        DENIED
    }

    /**
     * Map fields with custom objects
     * <p>
     *     Using the field, the correct mapper is determined and its defined action is executed, which returns an instance of the object specified as the field type
     * </p>
     * @param field the field to map
     * @param value the raw value to map into the field
     * @return an instance of the object specified as the field type
     */
    public Object customMapping(Field field, Object value) {
        switch (field.getName()) {
            case "interestGroup" -> {
                InterestGroup group = new InterestGroup();
                group.setName((String) value);
                return group;
            }
            default -> {
                return IEntity.super.customMapping(field);
            }

        }
    }
}
