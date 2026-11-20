package ch.frily.yubot.service.forummanager.entity;

import ch.frily.yubot.Client;
import ch.frily.yubot.EnvKey;
import ch.frily.yubot.database.IEntity;
import ch.frily.yubot.exception.InvalidStateException;
import ch.frily.yubot.exception.NotFoundException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.forums.ForumPost;
import org.jspecify.annotations.NonNull;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;

/**
 * Interest Entity
 * <p>
 *     Saves all data for a special interest
 * </p>
 * @since v2.5.2
 * @author Aliz Rose Frily
 */
@Slf4j
public class SpecialInterest implements IEntity {

    /** How many interests the same user can habe pending at the same time */
    public static final int MAX_SIMULTANEOUS_PENDING_INTERESTS = 2;

    @Getter
    @Setter
    private int id;

    @Getter
    @Setter
    private ThreadChannel post;

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
        log.info("mapping with value " + value);
        switch (field.getName()) {
            case "originalPoster" -> {
                if (Objects.nonNull(value)) {
                    return Client.getInstance().getClient().getUserById((String) value);
                }
                return null;
            }
            case "post" -> {
                if (Objects.nonNull(value)) {
                    return EnvKey.GUILD_YUSERVER.getAsGuild().getThreadChannelById((String) value);
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
        public static final String POST_ID = "post_id";

        public static String getTable() {
            return "special_interest";
        }
    }


}
