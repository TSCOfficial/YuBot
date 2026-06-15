package ch.frily.yubot.feature;

import ch.frily.yubot.embed.ClosureLogEmbed;
import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class Closure extends Feature {

    private static Closure instance;

    private static final List<Category> CATEGORYKEYS = Stream.of(
            EnvKey.CATEGORY_ANSAGEN,
            EnvKey.CATEGORY_LIVEVENTS,
            EnvKey.CATEGORY_COMMUNITYFLOOR,
            EnvKey.CATEGORY_VOICE
    ).map(EnvResolver::getCategoryById).toList();

    private static final List<Permission> PERMISSIONS = List.of(
            Permission.VIEW_CHANNEL
    );

    private static final List<Role> MOD_ROLES = Stream.of(
            EnvKey.ROLE_MODLEITUNG,
            EnvKey.ROLE_MODERATOR,
            EnvKey.ROLE_SERVERLEITUNG
    ).map(EnvResolver::getRoleById).toList();

    public static Closure getInstance() {
        if (instance == null) {
            instance = new Closure();
        }
        return instance;
    }

    public void triggerUpdate(){
        List<Member> activeMods = getActiveMods();

        log.debug(String.valueOf(activeMods.size()));

        boolean isOpen = !activeMods.isEmpty();

        toggleCategoryPermissions(isOpen);
        toggleServerClosedChannelPermissions(!isOpen);

        if (isOpen) {
            TextChannel lobbyChannel = EnvResolver.getChannelById(TextChannel.class, EnvKey.GUILD_YUSERVER, EnvKey.CHANNEL_LOBBY); // lobby channel
            Role role = EnvResolver.getRoleById(EnvKey.ROLE_ANTIFASHIST);
            lobbyChannel.sendMessage("Hey " + role.getAsMention() + "! Es ist Zeit zu quatschen ✨").queue();
        }


        // Logging
        TextChannel logChannel = EnvResolver.getChannelById(TextChannel.class, EnvKey.GUILD_YUSERVER, EnvKey.CHANNEL_CLOSURELOGS); // Log channel
        logChannel.sendMessageEmbeds(new ClosureLogEmbed(!activeMods.isEmpty()).build()).queue();
    }

    /**
     * Toggle the permissions of the "server-geschlossen" channel
     * @param isOpen True if the channel should be open (Inverted value of closure's isOpen value)
     */
    private void toggleServerClosedChannelPermissions(boolean isOpen) {
        Role everyoneRole = EnvResolver.getRoleById(EnvKey.ROLE_EVERYONE);
        TextChannel serverClosedChannel = EnvResolver.getChannelById(TextChannel.class, EnvKey.GUILD_YUSERVER, EnvKey.CHANNEL_SERVERGESCHLOSSEN);

        List<Permission> allowPerms = new ArrayList<>();
        List<Permission> denyPerms = new ArrayList<>();

        if (isOpen) {
            allowPerms.addAll(PERMISSIONS);
        } else {
            denyPerms.addAll(PERMISSIONS);
        }

        serverClosedChannel.getManager().putRolePermissionOverride(everyoneRole.getIdLong(), allowPerms, denyPerms).queue();
    }

    /**
     * Toggle the @everyone-holders category permissions - without progress
     * @param isOpen True if the categories should be open, false if they should be closed
     */
    private void toggleCategoryPermissions(boolean isOpen) {
        Role everyoneRole = EnvResolver.getRoleById(EnvKey.ROLE_EVERYONE);

        List<Permission> allowPerms = new ArrayList<>();
        List<Permission> denyPerms = new ArrayList<>();

        if (isOpen) {
            allowPerms.addAll(PERMISSIONS);
        } else {
            denyPerms.addAll(PERMISSIONS);
        }

        for (Category category : CATEGORYKEYS) {

            category.getManager()
                    .putRolePermissionOverride(everyoneRole.getIdLong(), allowPerms, denyPerms)
                    .complete();

            for (GuildChannel channel : category.getChannels()) {
                channel.getPermissionContainer()
                        .upsertPermissionOverride(everyoneRole)
                        .setDenied(denyPerms)
                        .setAllowed(allowPerms)
                        .complete();
            }
        }
    }

    public static List<Member> getActiveMods() {
        Role activeModRole = EnvResolver.getRoleById(EnvKey.ROLE_ACTIVEMOD);
        Guild guild = EnvResolver.getGuildById(EnvKey.GUILD_YUSERVER);
        List<Member> members = guild.getMembersWithRoles(activeModRole);
        log.debug(members.stream().map(Member::getEffectiveName).collect(Collectors.joining(", ")));
        return members;
    }

    /**
     * Check if a member is a moderator
     * @param member
     * @return True if they are a mod, false if not
     */
    public static boolean isMod(Member member) {
        AtomicBoolean value = new AtomicBoolean(false);
        MOD_ROLES.forEach(role -> {
            if (member.getRoles().contains(role)) {
                value.set(true);
            }
        });
        return value.get();
    }
}
