package ch.frily.yubot.listeners;

import ch.frily.yubot.exception.ExceptionHandler;
import ch.frily.yubot.feature.*;
import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
import ch.frily.yubot.util.Util;
import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

@Slf4j
public class OnMessageReceived extends ListenerAdapter {

    private static OnMessageReceived instance;

    public static OnMessageReceived getInstance(){
        if (instance == null) {
            instance = new OnMessageReceived();
        }
        return instance;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        try {
            if (event.getAuthor().isBot()) return;

            // Ticket funtions
            if (event.getChannel() instanceof TextChannel
                    && TicketManager.getInstance().isTicketchannel(event.getChannel().asTextChannel())) {
                Ticket ticket = TicketRepository.getTicketById(event.getChannel().getIdLong());

                // ticket claim function
                if (Util.isTeamMember(event.getMember())) {
                    ticket.claim(event.getMember());
                }

                // ticket owner activity
                if (ticket.isOwner(event.getMember())) {
                    TicketRepository.updateTicketLastActivityAt(ticket);
                }
            }

            // adtive-mod activity
            if (Util.isActiveMod(event.getMember())) {
                Closure.handleModActivity(event.getMember());
            }

            // Word-Chain game
            if (event.getChannel().getId().equals(EnvResolver.getString(EnvKey.CHANNEL_KETTENBRIEF))) {
                WordChainGame.handleWord(event);
            }
        } catch (Exception exception) {
            ExceptionHandler.handle(exception);
        }
    }
}
