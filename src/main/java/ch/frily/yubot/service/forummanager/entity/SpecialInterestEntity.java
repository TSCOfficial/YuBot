package ch.frily.yubot.service.forummanager.entity;

import ch.frily.yubot.Client;
import ch.frily.yubot.EnvKey;
import ch.frily.yubot.database.IEntity;
import ch.frily.yubot.database.Table;
import ch.frily.yubot.exception.InvalidStateException;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.User;
import org.jspecify.annotations.NonNull;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.Objects;
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
    private User originalPoster;

    public enum RequestStatus {
        REQUESTED,
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
}
