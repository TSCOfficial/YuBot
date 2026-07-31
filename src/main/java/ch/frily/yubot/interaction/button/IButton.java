package ch.frily.yubot.interaction.button;

import ch.frily.yubot.util.EnvKey;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public interface IButton {

    Map<String, String> arguments = new TreeMap<>();

    /**
     * Define the ID of the button
     * @return
     */
    String defineId();

    /**
     * Get the ID of the button
     * <p>This method uses the defined-ID from {@link #defineId()} and populates the arguments on the ID.
     * <pre><code>
     *     my-btn-id?arg1=value1&arg2=value2
     * </code></pre></p>
     * To retrieve and dispatch the button, split at <code>?</code>.
     * @return
     */
    default String getId(){
        return defineId() + "?" + arguments.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .reduce((a, b) -> a + "&" + b)
                .orElse("");
    };

    default void addArgument(String key, String value){
        arguments.put(key, value);
    }

    default String getLabel() {
        return null;
    };

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

    default Button build(){
        String idOrUrl = getId();
        if (getStyle() == ButtonStyle.LINK && getUrl() != null) {
            idOrUrl = getUrl();
        }
        Button button = Button.of(getStyle(), idOrUrl, getLabel(), getEmoji());
        if (isDisabled()) {
            button.asDisabled();
        }
        return button;
    }

    void execute(@NotNull ButtonInteractionEvent event) throws SQLException, ClassNotFoundException;

    /**
     * Get the allowed roles of an interaction.
     * <p>
     * Allowed roles are roles that a user needs to execute the interaction. (The user needs to have at least one of these roles)
     * <p>
     * <b>How to define roles using {@link EnvKey}:</b>
     * <pre><code>
     *     Stream.of(
     *             EnvKey.ROLE_MODLEITUNG,
     *             EnvKey.ROLE_MODERATOR,
     *     ).map(EnvResolver::getRoleById).toList();
     * </code></pre>
     * <p>
     * If left empty, everyone can execute the interaction.
     * @return
     */
    default List<Role> getAllowedRoles() {
        return List.of();
    }
}
