package ch.frily.yubot.feature;

import ch.frily.yubot.embed.ClosureActivityRequestEmbed;
import ch.frily.yubot.embed.ClosureLogEmbed;
import ch.frily.yubot.exception.ThrowingConsumer;
import ch.frily.yubot.interaction.button.btn.ActiveModActivityProveBtn;
import ch.frily.yubot.interaction.button.btn.ActiveModActivityRejectBtn;
import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
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
    private static final int MIN_INACTIVITY_TIME = 1;

    /** How long an activity request stays open till it gets automatically rejected [in minutes]*/
    @Getter
    private static final int MAX_ACTIVITY_REQUEST_RESPONSE_TIME = 1;

    public static Closure getInstance() {
        if (instance == null) {
            instance = new Closure();
        }
        return instance;
    }

    /**
     * Trigger the closure system.
     * <p></p>
     * This synchronizes the Database, handles closure & opening
     * @throws SQLException
     */
    public void triggerUpdate() throws SQLException, ClassNotFoundException {
        List<Member> activeMods = getActiveMods();

        boolean isOpen = !activeMods.isEmpty();

        syncDatabaseMods();

        toggleCommunityPermission(isOpen);
        toggleServerClosedInfoChannelPermissions(!isOpen);

        if (isOpen) {
            TextChannel lobbyChannel = EnvResolver.getChannelById(TextChannel.class, EnvKey.GUILD_YUSERVER, EnvKey.CHANNEL_LOBBY); // lobby channel
            Role role = EnvResolver.getRoleById(EnvKey.ROLE_ANTIFASHIST);
            lobbyChannel.sendMessage("Hey " + role.getName() + "! Es ist Zeit zu quatschen ✨").queue();
        }


        // Logging
        TextChannel logChannel = EnvResolver.getChannelById(TextChannel.class, EnvKey.GUILD_YUSERVER, EnvKey.CHANNEL_CLOSURELOGS); // Log channel
        logChannel.sendMessageEmbeds(new ClosureLogEmbed(!activeMods.isEmpty()).build()).queue();
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
    private void toggleCommunityPermission(boolean isOpen) {
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
    private static void syncDatabaseMods() throws SQLException, ClassNotFoundException {
        // All mods based on the database data
        List<ActiveMod> currentModsInDatabase = ClosureRepository.getModerators();

        List<Member> activeMods = getActiveMods();

        // IDs of mods currently stored in the database
        List<Long> databaseModIds = currentModsInDatabase.stream()
                .map(ActiveMod::member)
                .map(Member::getIdLong)
                .toList();

        // IDs of mods that actually hold the active-mod role
        List<Long> activeModIds = activeMods.stream()
                .map(Member::getIdLong)
                .toList();

        List<Member> modsToRemove = currentModsInDatabase.stream()
                .map(ActiveMod::member)
                .filter(member -> !activeModIds.contains(member.getIdLong()))
                .toList();

        List<Member> modsToAdd = activeMods.stream()
                .filter(member -> !databaseModIds.contains(member.getIdLong()))
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

    public static void handleModActivity(Member member) throws SQLException, ClassNotFoundException {
        ActiveMod activeMod = ClosureRepository.getModerator(member);
        log.debug("RequestMsg Id: " +activeMod.activityRequestMessageId() + activeMod.activityRequestMessageId().getClass());
        if (activeMod.activityRequestMessageId() != null && activeMod.activityRequestMessageId() != 0) { // automatically accept an active activity-prove-request
            TextChannel channel = EnvResolver.getChannelById(TextChannel.class, EnvKey.GUILD_YUSERVER, EnvKey.CHANNEL_MODINTERN);
            channel.deleteMessageById(activeMod.activityRequestMessageId()).queue();
        }
        ClosureRepository.updateModeratorActivity(member);
    }

    /**
     * When the active-mod is inactive for an amount of time, send a request to prove they're active
     * @param moderator mod-record {@link ActiveMod}
     */
    public static void requestActivityProve(ActiveMod moderator) {
        TextChannel channel = EnvResolver.getChannelById(TextChannel.class, EnvKey.GUILD_YUSERVER, EnvKey.CHANNEL_MODINTERN);
        if (moderator.activityRequestedAt() == null) {

            ActionRow actionrow = ActionRow.of(new ActiveModActivityProveBtn().build(), new ActiveModActivityRejectBtn().build());

            channel.sendMessageEmbeds(new ClosureActivityRequestEmbed(moderator).build()).setComponents(actionrow).queue(message -> ThrowingConsumer.wrap(null, _ -> {
                ActiveMod updatedActiveMod = new ActiveMod(moderator.member(), moderator.lastActivityAt(), LocalDateTime.now(), message.getIdLong());
                ClosureRepository.updateModerator(updatedActiveMod);
            }));
        } else {
            handleActivityProveTimeout(moderator);
        }
    }

    /**
     * Handle an unresponded activity request
     * @param moderator
     */
    private static void handleActivityProveTimeout(ActiveMod moderator) {
        if (moderator.activityRequestedAt().isBefore(LocalDateTime.now().minusMinutes(Closure.getMAX_ACTIVITY_REQUEST_RESPONSE_TIME()))) {
            Guild guild = EnvResolver.getGuildById(EnvKey.GUILD_YUSERVER);
            TextChannel channel = guild.getTextChannelById(1516042711273046087L);

            long epochTime = moderator.lastActivityAt().atZone(EnvResolver.getZoneId()).toEpochSecond();

            channel.retrieveMessageById(moderator.activityRequestMessageId()).queue(message -> {
                message.editMessage(
                        String.format("❌ %s hat die Aktivitätsbestätigungsanfrage ignoriert.\n-# Zuletzt erkannte Aktivität war um <t:%d:T> (<t:%d:R>).", moderator.member().getAsMention(), epochTime, epochTime)
                ).setComponents().setEmbeds().queue();
            });


            guild.removeRoleFromMember(moderator.member(), EnvResolver.getRoleById(EnvKey.ROLE_ACTIVEMOD)).queue();
        }
    }
}
