package ch.frily.yubot.interaction.contextmenu;

import ch.frily.yubot.util.EnvKey;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.interactions.commands.Command;

import java.util.List;

public interface IContextMenu {

    String getName();

    Command.Type getType();

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
    }}
