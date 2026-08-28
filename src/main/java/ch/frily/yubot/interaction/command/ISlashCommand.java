package ch.frily.yubot.interaction.command;

import ch.frily.yubot.util.EnvKey;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface ISlashCommand {

    /**
     * Get the name of a slashcommand.
     *
     * @return a {@link String}
     */
    String getName();


    /**
     * Get the description of a slashcommand.
     *
     * @return a {@link String}
     */
    String getDescription();

    /**
     * Execute a slashcommand by its interaction event.<br>
     * @param event The {@link SlashCommandInteractionEvent}
     */
    void execute(@NotNull SlashCommandInteractionEvent event) throws SQLException, ClassNotFoundException;

    /**
     * Get the options and choices of a slashcommand.
     *
     * @return a {@link List} of {@link OptionData}.
     */
    default List<OptionData> getOptions() {
        return List.of();
    };

    /**
     * Get the autocompletion of a slashcommand.
     * @return a {@link Map} of a {@link String} & a {@link List}.
     */
    default Map<String, List<?>> getAutocomplete(CommandAutoCompleteInteractionEvent event) {
        return Map.of();
    };

    /**
     * Get the default permissions of a slashcommand
     * <br>
     * Default permissions are discord-permissions that a user needs to execute the command.
     * @return a list of {@link Permission}
     */
    default List<Permission> getDefaultPermissions() {
        return List.of();
    }

    /**
     * Get the allowed roles of a slashcommand.
     * <p>
     * Allowed roles are roles that a user needs to execute the command. (The user needs to have at least one of these roles)
     * <p>
     * <b>How to define roles using {@link EnvKey}:</b>
     * <pre><code>
     *     Stream.of(
     *             EnvKey.ROLE_MODLEITUNG,
     *             EnvKey.ROLE_MODERATOR,
     *     ).map(EnvResolver::getRoleById).toList();
     * </code></pre>
     * <p>
     * If left empty, everyone can execute the command. (except defined otherwise in {@link #getDefaultPermissions()})
     * @return
     */
    default List<Role> getAllowedRoles() {
        return List.of();
    }
}
