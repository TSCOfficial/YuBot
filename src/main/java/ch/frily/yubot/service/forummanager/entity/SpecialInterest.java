package ch.frily.yubot.service.forummanager.entity;

import ch.frily.yubot.Client;
import ch.frily.yubot.database.IEntity;
import ch.frily.yubot.exception.InvalidStateException;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.User;
import org.jspecify.annotations.NonNull;

import java.lang.reflect.Field;
import java.util.Objects;

/**
 * Interest Entity
 * <p>
 *     Saves all data for a special interest
 * </p>
 * @since v2.5.2
 * @author Aliz Rose Frily
 */
public class SpecialInterest implements IEntity {

    /** How many interests the same user can habe pending at the same time */
    public static final int MAX_SIMULTANEOUS_PENDING_INTERESTS = 2;

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
    private User originalPoster;

    public enum RequestStatus {
        PENDING,
        APPROVED,
        DENIED
    }

    @Override
    public Object customMapping(@NonNull Field field, Object value) {
        switch (field.getName()) {
            case "originalPoster" -> {
                if (Objects.nonNull(value)) {
                    User poster = Client.getInstance().getClient().getUserById((String) value);
                    return poster;
                }
                return null;
            }
            default -> {
                throw new InvalidStateException(String.format("Could not resolve customMapper for field: %s", field.getName()));
            }

        }
    }

    public static class Table {
        public static final String ID = "id";
        public static final String NAME = "name";
        public static final String DESCRIPTION = "description";
        public static final String REQUEST_STATUS = "request_status";
        public static final String ORIGINAL_POSTER_ID = "original_poster_id";

        public static String getTable() {
            return "special_interest";
        }
    }


}
