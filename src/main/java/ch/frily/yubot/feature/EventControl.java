package ch.frily.yubot.feature;

import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.IPermissionHolder;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Controls the visibility of the #community-events channel
 */
public class EventControl extends Feature {

    private static EventControl instance;

    private static final List<Permission> PERMISSIONS = List.of(
            Permission.VIEW_CHANNEL
    );

    public EventControl() {
        addPermission("*", List.of(EnvKey.ROLE_EVENT, EnvKey.ROLE_EVENTLEITUNG, EnvKey.ROLE_SERVERLEITUNG), "Nur das Eventteam darf dies ausführen.");
    }

    public static EventControl getInstance() {
        if (instance == null) {
            instance = new EventControl();
        }
        return instance;
    }

    /**
     * Toogle channels Permissions
     * @param isOpen Whether the channel should be open (true) or closed (false)
     */
    public void toggleChannelPermissions(boolean isOpen) {
        Role everyoneRole = EnvResolver.getRoleById(EnvKey.ROLE_EVERYONE);
        TextChannel channel = EnvResolver.getChannelById(TextChannel.class, EnvKey.GUILD_YUSERVER, EnvKey.CHANNEL_COMMUNITYEVENTS);

        List<Permission> allowPerms = new ArrayList<>();
        List<Permission> denyPerms = new ArrayList<>();

        if (isOpen) {
            allowPerms.addAll(PERMISSIONS);
        } else {
            denyPerms.addAll(PERMISSIONS);
        }

        channel.getManager().putRolePermissionOverride(everyoneRole.getIdLong(), allowPerms, denyPerms).queue();
    }
}
