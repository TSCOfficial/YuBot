package ch.frily.yubot.service.forummanager.entity;

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
}
