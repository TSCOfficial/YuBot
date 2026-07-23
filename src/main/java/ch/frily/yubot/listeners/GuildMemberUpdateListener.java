package ch.frily.yubot.listeners;

import ch.frily.yubot.exception.ExceptionHandler;
import ch.frily.yubot.feature.Closure;
import ch.frily.yubot.feature.DynamicMessageList;
import ch.frily.yubot.feature.TicketRepository;
import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
import ch.frily.yubot.util.Util;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.sql.SQLException;

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

                DynamicMessageList.TEAMLIST.update();
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

                DynamicMessageList.TEAMLIST.update();
            }
        } catch (Exception exception) {
            ExceptionHandler.handle(exception);
        }
    }

    @Override
    public void onGuildMemberRemove(@NonNull GuildMemberRemoveEvent event) {
        try {
            if (event.getMember() != null && event.getMember().getUser() != null) {
                TicketRepository.getTicketsByUser(event.getMember().getUser()).forEach(ticket -> {
                    try {
                        ticket.getChannel().sendMessage("Der Benutzer hat den Server verlassen. Das Ticket wird geschlossen.").queue();
                        ticket.close(null, true);
                    } catch (Exception exception) {
                        ExceptionHandler.handle(exception);
                    }
                });
            }

        } catch (Exception exception) {
            ExceptionHandler.handle(exception);
        }
    }
}
