package ch.frily.yubot.listeners;

import ch.frily.yubot.exception.ExceptionHandler;
import ch.frily.yubot.feature.Closure;
import ch.frily.yubot.feature.Ticket;
import ch.frily.yubot.feature.TicketManager;
import ch.frily.yubot.feature.TicketRepository;
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

            // Ticket claim funtion
            if (event.getChannel() instanceof TextChannel
                    && TicketManager.getInstance().isTicketchannel(event.getChannel().asTextChannel())
                    && Util.isTeamMember(event.getMember())) {
                Ticket ticket = TicketRepository.getTicketById(event.getChannel().getIdLong());
                ticket.claim(event.getMember());
            }

            if (Util.isActiveMod(event.getMember())) {
                Closure.handleModActivity(event.getMember());
                log.debug("Mod activity detected");
            }
        } catch (Exception exception) {
            ExceptionHandler.handle(exception);
        }
    }
}
