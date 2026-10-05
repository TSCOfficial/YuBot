package ch.frily.yubot.interaction.button;

import ch.frily.yubot.util.EnvKey;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.List;

public interface IButton {
    default String getLabel() {
        return null;
    }

    ButtonStyle getStyle();

    /**
     * define URL if style URL
     */
    default String getUrl(){
        return null;
    }

    default boolean isDisabled(){
        return false;
    }

    default EmojiUnion getEmoji(){
        return null;
    }

    void execute(@NotNull ButtonInteractionEvent event) throws SQLException;

    /**
     * Get the allowed roles of an interaction.
     * <p>
     * Allowed roles are roles that a user needs to execute the interaction. (The user needs to have at least one of these roles)
     * </p>
     * <p>
     *     <b>How to define roles using {@link EnvKey}:</b>
     *     <pre><code>
     *         Stream.of(
     *             EnvKey.ROLE_MODLEITUNG,
     *             EnvKey.ROLE_MODERATOR,
     *         ).map(EnvResolver::getRoleById).toList();
     *      </code></pre>
     * <p>
     * If left empty, everyone can execute the interaction.
     * @return a list of {@link Role}s that are allowed to use the button. Administrators bypass this check.
     */
    default List<Role> getAllowedRoles() {
        return List.of();
    }
}
