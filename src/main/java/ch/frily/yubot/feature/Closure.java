package ch.frily.yubot.feature;

import ch.frily.yubot.embed.ClosureActivityRequestEmbed;
import ch.frily.yubot.embed.ClosureLogEmbed;
import ch.frily.yubot.exception.ThrowingConsumer;
import ch.frily.yubot.interaction.button.btn.ActiveModActivityProveBtn;
import ch.frily.yubot.interaction.button.btn.ActiveModActivityRejectBtn;
import ch.frily.yubot.interaction.button.btn.DeleteActivityRequestMsgBtn;
import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.io.IOException;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

@Slf4j
public class Closure {

    private static Closure instance;

    private static final List<Permission> PERMISSIONS = List.of(
            Permission.MESSAGE_SEND,
            Permission.VIEW_CHANNEL
    );

    private static final List<Role> MOD_ROLES = Stream.of(
            EnvKey.ROLE_MODLEITUNG,
            EnvKey.ROLE_MODERATOR,
            EnvKey.ROLE_SERVERLEITUNG
    ).map(EnvResolver::getRoleById).toList();

    /** How long a person needs to be inactive to trigger an activity request [in minutes] */
    @Getter
    private static final int MIN_INACTIVITY_TIME = 30;

    /** How long an activity request stays open till it gets automatically rejected [in minutes]*/
    @Getter
    private static final int MAX_NORMAL_ACTIVITY_REQUEST_RESPONSE_TIME = 10;
    
    /** How long an activity request stays open, if the mod is the only active mod [in minutes] */
    private static final int PING_MODS_AFTER_ACTIVITY_REQUEST_IF_ALONE = 5;

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
    public void triggerUpdate() throws SQLException, ClassNotFoundException, IOException {
        List<Member> activeMods = getActiveMods();

        boolean isOpen = !activeMods.isEmpty();

        syncDatabaseMods();

        toggleCommunityPermission(isOpen);
        toggleServerClosedInfoChannelPermissions(isOpen);

        Role everyoneRole = EnvResolver.getRoleById(EnvKey.ROLE_EVERYONE);
        TextChannel logChannel = EnvResolver.getChannelById(TextChannel.class, EnvKey.GUILD_YUSERVER, EnvKey.CHANNEL_CLOSURELOGS);
        Guild guild = EnvResolver.getGuildById(EnvKey.GUILD_YUSERVER);
        TextChannel lobbyChannel = EnvResolver.getChannelById(TextChannel.class, EnvKey.GUILD_YUSERVER, EnvKey.CHANNEL_LOBBY);

        // Opening the server
        if (isOpen && !everyoneRole.getPermissions().contains(Permission.VIEW_CHANNEL)) {
            DynamicMessageList.TICKET_PANEL.update();
            Role mentionRole = EnvResolver.getRoleById(EnvKey.ROLE_EROEFFNUNGSPING);
            lobbyChannel.sendMessage(String.format("%s, es ist Zeit zu quatschen ✨", mentionRole.getAsMention())).queue();

            logChannel.sendMessageEmbeds(new ClosureLogEmbed(true).build()).queue();
            guild.getManager().setIcon(Icon.from(getClass().getResourceAsStream("/icon/server-icon.png"))).queue();
            guild.getManager().setBanner(Icon.from(getClass().getResourceAsStream("/icon/server-banner.png"))).queue();
        }
        // Closing the server
        if (!isOpen && everyoneRole.getPermissions().contains(Permission.VIEW_CHANNEL)) {
            DynamicMessageList.TICKET_PANEL.update();
            logChannel.sendMessageEmbeds(new ClosureLogEmbed(false).build()).queue();
            lobbyChannel.sendMessage(String.format("""
                    ## %s
                    Der server ist nun geschlossen.
                    -# Es gibt keine aktiven Moderatoren mehr.
                    """, EnvResolver.getChannelById(TextChannel.class, EnvKey.GUILD_YUSERVER, EnvKey.CHANNEL_SERVERGESCHLOSSEN).getAsMention())).queue();
            guild.getManager().setIcon(Icon.from(getClass().getResourceAsStream("/icon/server-icon-closed.png"))).queue();
            guild.getManager().setBanner(Icon.from(getClass().getResourceAsStream("/icon/server-banner-closed.png"))).queue();
        }
    }

    /**
     * Toggle the permissions of the "server-geschlossen" channel
     * @param isOpen True if the channel should be open
     */
    private void toggleServerClosedInfoChannelPermissions(boolean isOpen) {
        Role everyoneRole = EnvResolver.getRoleById(EnvKey.ROLE_EVERYONE);
        TextChannel serverClosedChannel = EnvResolver.getChannelById(TextChannel.class, EnvKey.GUILD_YUSERVER, EnvKey.CHANNEL_SERVERGESCHLOSSEN);

        List<Permission> allowPerms = new ArrayList<>();
        List<Permission> denyPerms = new ArrayList<>();

        if (isOpen) {
            denyPerms.add(Permission.VIEW_CHANNEL);
        } else {
            allowPerms.add(Permission.VIEW_CHANNEL);
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
        List<ActiveMod> currentModsInDatabase = ActiveModRepository.getModerators();

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
            ActiveModRepository.deleteModerator(member);
        }

        for (Member member : modsToAdd) {
            ActiveModRepository.createModerator(member);
        }
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
        ActiveMod activeMod = ActiveModRepository.getModerator(member);
        if (activeMod.activityRequestMessageId() != 0) { // automatically accept an active activity-prove-request
            TextChannel channel = EnvResolver.getChannelById(TextChannel.class, EnvKey.GUILD_YUSERVER, EnvKey.CHANNEL_MODINTERN);
            channel.deleteMessageById(activeMod.activityRequestMessageId()).queue();
        }
        ActiveModRepository.updateModeratorActivity(member);
        deleteRequestedAttentionMessages();

    }

    /**
     * When the active-mod is inactive for an amount of time, send a request to prove they're active
     * @param moderator mod-record {@link ActiveMod}
     */
    public static void requestActivityProve(ActiveMod moderator) throws SQLException, ClassNotFoundException {
        TextChannel channel = EnvResolver.getChannelById(TextChannel.class, EnvKey.GUILD_YUSERVER, EnvKey.CHANNEL_MODINTERN);
        if (moderator.activityRequestedAt() == null) {

            ActionRow actionrow = ActionRow.of(new ActiveModActivityProveBtn().build(), new ActiveModActivityRejectBtn().build());

            channel.sendMessage(moderator.member().getAsMention())
                    .addEmbeds(new ClosureActivityRequestEmbed(moderator).build())
                    .setComponents(actionrow)
                    .queue(ThrowingConsumer.wrap(null, message -> {
                        ActiveMod updatedActiveMod = new ActiveMod(moderator.member(), moderator.lastActivityAt(), LocalDateTime.now(), message.getIdLong(), moderator.activityRequestMessageId());
                        ActiveModRepository.updateModerator(updatedActiveMod);
                    }
                    ));
            ActiveModTrackingRepository.incrementTotalActivityRequestCount(moderator.member());
        } else {
            handleActivityProveTimeout(moderator);
        }
    }

    /**
     * Handle an unresponded activity request
     * @param moderator
     */
    private static void handleActivityProveTimeout(ActiveMod moderator) throws SQLException, ClassNotFoundException {
        if (getActiveMods().size() == 1) {
            if (moderator.activityRequestedAt().isBefore(LocalDateTime.now().minusMinutes(Closure.PING_MODS_AFTER_ACTIVITY_REQUEST_IF_ALONE)) && moderator.requestedAttentionMessageId() == 0) {
                TextChannel channel = EnvResolver.getChannelById(TextChannel.class, EnvKey.GUILD_YUSERVER, EnvKey.CHANNEL_MODINTERN);
                channel.sendMessage(String.format("""
                                %s
                                ⚠️ %s hat bislang noch nicht auf die Aktivitätsbestätigungsanfrage geantwortet.
                                Der Server würde <t:%d:R> schliessen.
                                -# Halte du den Server offen indem du dich </activemod opt-in:1518320360834207866> machst.
                                """,
                        EnvResolver.getRoleById(EnvKey.ROLE_MODERATOR).getAsMention(),
                        moderator.member().getAsMention(),
                        Instant.now().plus(5, java.time.temporal.ChronoUnit.MINUTES).toEpochMilli() / 1000
                )).queue(ThrowingConsumer.wrap(null, message -> {
                    ActiveMod updatedActiveMod = new ActiveMod(moderator.member(), moderator.lastActivityAt(), moderator.activityRequestedAt(), moderator.activityRequestMessageId(), message.getIdLong());
                    ActiveModRepository.updateModerator(updatedActiveMod);
                }));
            }
        }
        if (moderator.activityRequestedAt().isBefore(LocalDateTime.now().minusMinutes(Closure.MAX_NORMAL_ACTIVITY_REQUEST_RESPONSE_TIME))) {
            Guild guild = EnvResolver.getGuildById(EnvKey.GUILD_YUSERVER);
            TextChannel channel = guild.getTextChannelById(1516042711273046087L);

            long epochTime = moderator.lastActivityAt().atZone(EnvResolver.getZoneId()).toEpochSecond();

            channel.retrieveMessageById(moderator.activityRequestMessageId()).queue(message -> {
                message.editMessage(
                        String.format("❌ %s hat die Aktivitätsbestätigungsanfrage ignoriert.\n-# Zuletzt erkannte Aktivität war um <t:%d:T> (<t:%d:R>).", moderator.member().getAsMention(), epochTime, epochTime)
                ).setComponents(ActionRow.of(new DeleteActivityRequestMsgBtn().build())).setEmbeds().queue();
            });

            guild.removeRoleFromMember(moderator.member(), EnvResolver.getRoleById(EnvKey.ROLE_ACTIVEMOD)).queue();

            // Send close message in modchat when no other mods are active
            if (getActiveMods().isEmpty()) {
                requestAttentionMessageIgnored(moderator.requestedAttentionMessageId());
            }

            ActiveModTrackingRepository.incrementTotalActivityRequestCount(moderator.member());
        }
    }

    /**
     * If someone gave their attention (opt-in, accepted activity-prove-request, sent message) delete the message
     */
    public static void deleteRequestedAttentionMessages() throws SQLException, ClassNotFoundException {
        Guild guild = EnvResolver.getGuildById(EnvKey.GUILD_YUSERVER);
        List<ActiveMod> activeMods = ActiveModRepository.getModeratorsByRequestedAttentionMessageId();
        activeMods.forEach(activeMod -> {
            TextChannel channel = guild.getTextChannelById(1516042711273046087L);
            channel.retrieveMessageById(activeMod.requestedAttentionMessageId()).queue(message -> {
                message.delete().queue();
            });
        });
    }

    /**
     * If no one gave their attention (opt-in, accepted activity-prove-request, sent message) delete the message and close the server
     */
    public static void requestAttentionMessageIgnored(long messageId) {
        Guild guild = EnvResolver.getGuildById(EnvKey.GUILD_YUSERVER);
        TextChannel channel = guild.getTextChannelById(1516042711273046087L);
        channel.retrieveMessageById(messageId).queue(message -> {
            message.editMessage(String.format("""
                    Es hat sich niemand gemeldet. Der Server wird nun geschlossen.
                    """)).queue();
        });
    }
}
