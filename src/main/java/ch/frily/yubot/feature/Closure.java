package ch.frily.yubot.feature;

import ch.frily.yubot.container.ClosureActivityRequestContainer;
import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

@Slf4j
public class Closure extends Feature {

    private static Closure instance;

    private static final List<Permission> PERMISSIONS = List.of(
            Permission.VIEW_CHANNEL
    );

    private static final List<Role> MOD_ROLES = Stream.of(
            EnvKey.ROLE_MODLEITUNG,
            EnvKey.ROLE_MODERATOR,
            EnvKey.ROLE_SERVERLEITUNG
    ).map(EnvResolver::getRoleById).toList();

    /** How long a person needs to be inactive to trigger an activity request [in minutes] */
    @Getter
    private static final int MIN_INACTIVITY_TIME = 5;

    /** How long a activity request stays open till it gets automatically rejected [in minutes]*/
    @Getter
    private static final int MAX_ACTIVITY_REQUEST_RESPONSE_TIME = 1;

    public static Closure getInstance() {
        if (instance == null) {
            instance = new Closure();
        }
        return instance;
    }

    public void triggerUpdate() throws SQLException {
        List<Member> activeMods = getActiveMods();

        boolean isOpen = !activeMods.isEmpty();

        // Update database
        updateActiveMods();

//        toggleCategoryPermissions(isOpen);
//        toggleServerClosedInfoChannelPermissions(!isOpen);
//
//        if (isOpen) {
//            TextChannel lobbyChannel = EnvResolver.getChannelById(TextChannel.class, EnvKey.GUILD_YUSERVER, EnvKey.CHANNEL_LOBBY); // lobby channel
//            Role role = EnvResolver.getRoleById(EnvKey.ROLE_ANTIFASHIST);
//            lobbyChannel.sendMessage("Hey " + role.getName() + "! Es ist Zeit zu quatschen ✨").queue();
//        }
//
//
//        // Logging
//        TextChannel logChannel = EnvResolver.getChannelById(TextChannel.class, EnvKey.GUILD_YUSERVER, EnvKey.CHANNEL_CLOSURELOGS); // Log channel
//        logChannel.sendMessageEmbeds(new ClosureLogEmbed(!activeMods.isEmpty()).build()).queue();
    }

    /**
     * Toggle the permissions of the "server-geschlossen" channel
     * @param isOpen True if the channel should be open (Inverted value of closure's isOpen value)
     */
    private void toggleServerClosedInfoChannelPermissions(boolean isOpen) {
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

        if (isOpen) {
            everyoneRole.getManager().givePermissions(PERMISSIONS).queue();
        } else {
            everyoneRole.getManager().revokePermissions(PERMISSIONS).queue();
            kickMembersFromVoice();
        }
    }

    private void kickMembersFromVoice() {
        Guild guild = EnvResolver.getGuildById(EnvKey.GUILD_YUSERVER);
        guild.getVoiceStates().forEach(voiceState -> {
            if (voiceState.getChannel().getParentCategory() == null || voiceState.getChannel().getParentCategory() != EnvResolver.getCategoryById(EnvKey.CATEGORY_TEAMBEREICH) ) {
                guild.moveVoiceMember(voiceState.getMember(), null).queue();
            }
        });
    }

    /**
     * Get all currently active moderators (active: based if they have their role or not)
     * @return
     */
    public static List<Member> getActiveMods() {
        Role activeModRole = EnvResolver.getRoleById(EnvKey.ROLE_ACTIVEMOD);
        Guild guild = EnvResolver.getGuildById(EnvKey.GUILD_YUSERVER);
        return guild.getMembersWithRoles(activeModRole);
    }

    /**
     * Add missing mods and remove still saved active-mods
     */
    private static void updateActiveMods() throws SQLException {
        // All mods based on the database data
        List<ActiveMod> currentModsInDatabase = ClosureRepository.getModerators();

        List<Member> activeMods = getActiveMods();

        List<Member> modsToRemove = currentModsInDatabase.stream()
                .map(ActiveMod::member)
                .filter(member -> !activeMods.contains(member))
                .toList();

        List<Member> modsToAdd = activeMods.stream()
                .filter(member -> !currentModsInDatabase.contains(member))
                .toList();

        for (Member member : modsToRemove) {
            ClosureRepository.deleteModerator(member);
            log.info("Moderator mit der ID {} wurde aus der aktiven Liste entfernt.", member.getIdLong());
        }

        for (Member member : modsToAdd) {
            ClosureRepository.createModerator(member);
            log.info("Moderator mit der ID {} wurde zur aktiven Liste hinzugefügt.", member.getIdLong());
        }

        log.info("Aktualisierung der aktiven Moderatoren abgeschlossen!");

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

    /**
     * When the active-mod is inactive for an amount of time, send a request to prove they're active
     * @param moderator mod-record {@link ActiveMod}
     */
    public static void requestActivityProve(ActiveMod moderator) {
        //TextChannel channel = EnvResolver.getChannelById(TextChannel.class, EnvKey.GUILD_YUSERVER, EnvKey.CHANNEL_MODINTERN);
        TextChannel channel = EnvResolver.getGuildById(EnvKey.GUILD_YUSERVER).getTextChannelById(1516079055693287545L);
        if (moderator.activityRequestedAt() == null) {
            log.debug("Requesting activity");
            channel.sendMessageComponents(new ClosureActivityRequestContainer(moderator).build()).useComponentsV2().queue(message -> {
                ActiveMod updatedActiveMod = new ActiveMod(moderator.member(), moderator.lastActivityAt(), LocalDateTime.now(), message.getIdLong());
                ClosureRepository.updateModerator(updatedActiveMod);
            });
        } else {
            log.debug("Activity request already sent");
            handleActivityProveTimeout(moderator);
        }
    }

    /**
     * Handle an unresponded activity request
     * @param moderator
     */
    private static void handleActivityProveTimeout(ActiveMod moderator) {
        if (moderator.activityRequestedAt().isBefore(LocalDateTime.now().minusMinutes(Closure.getMAX_ACTIVITY_REQUEST_RESPONSE_TIME()))) {
            log.debug("cancelling activity request");
            TextChannel channel = EnvResolver.getGuildById(EnvKey.GUILD_YUSERVER).getTextChannelById(1516079055693287545L);
            channel.sendMessage("Activity request failed. Removing from active mods.").queue();
        }
    }
}
