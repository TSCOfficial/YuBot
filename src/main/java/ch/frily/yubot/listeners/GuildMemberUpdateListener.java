package ch.frily.yubot.listeners;

import ch.frily.yubot.Client;
import ch.frily.yubot.exception.ExceptionHandler;
import ch.frily.yubot.feature.Closure;
import ch.frily.yubot.feature.Teamlist;
import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
import ch.frily.yubot.util.Util;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberUpdateEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateOnlineStatusEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

@Slf4j
public class GuildMemberUpdateListener extends ListenerAdapter {

    private static GuildMemberUpdateListener instance;

    public static GuildMemberUpdateListener getInstance() {
        if (instance == null) {
            instance = new GuildMemberUpdateListener();
        }
        return instance;
    }

    @Override
    public void onGuildMemberRoleAdd(@NotNull GuildMemberRoleAddEvent event) {
        try {
            if (Util.isTeamMember(event.getMember())) {

                if (event.getRoles().contains(EnvResolver.getRoleById(EnvKey.ROLE_ACTIVEMOD))) {
                    Closure.getInstance().triggerUpdate();
                }

                MessageEmbed embed = Teamlist.getInstance().generateEmbed();

                TextChannel channel = (TextChannel) EnvResolver.getChannelById(TextChannel.class, EnvKey.GUILD_YUSERVER, EnvKey.CHANNEL_DASTEAM);

                EnvResolver.getMessageById(event.getGuild().getIdLong(), channel.getIdLong(), channel.getLatestMessageIdLong()).thenAccept(message -> {
                    message.editMessageEmbeds(embed).queue();
                }).exceptionally(error -> {
                    channel.sendMessage("").addEmbeds(embed).queue();
                    return null;
                });
            }
        } catch (Exception exception) {
            ExceptionHandler.handle(exception);
        }
    }

    @Override
    public void onGuildMemberRoleRemove(@NotNull GuildMemberRoleRemoveEvent event) {
        try {
            if (Util.isTeamMember(event.getMember())) {
                if (event.getRoles().contains(EnvResolver.getRoleById(EnvKey.ROLE_ACTIVEMOD))) {
                    Closure.getInstance().triggerUpdate();
                }

                MessageEmbed embed = Teamlist.getInstance().generateEmbed();

                TextChannel channel = (TextChannel) EnvResolver.getChannelById(TextChannel.class, EnvKey.GUILD_YUSERVER, EnvKey.CHANNEL_DASTEAM);

                EnvResolver.getMessageById(event.getGuild().getIdLong(), channel.getIdLong(), channel.getLatestMessageIdLong()).thenAccept(message -> {
                    message.editMessageEmbeds(embed).queue();
                }).exceptionally(error -> {
                    channel.sendMessage("").addEmbeds(embed).queue();
                    return null;
                });
            }
        } catch (Exception exception) {
            ExceptionHandler.handle(exception);
        }
    }


}
