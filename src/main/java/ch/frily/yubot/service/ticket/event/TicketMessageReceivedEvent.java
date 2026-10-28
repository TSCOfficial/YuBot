package ch.frily.yubot.service.ticket.event;

import ch.frily.yubot.database.repository.TicketRepository;
import ch.frily.yubot.event.IEvent;
import ch.frily.yubot.service.ticket.Ticket;
import ch.frily.yubot.service.ticket.TicketService;
import ch.frily.yubot.util.Util;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.io.IOException;
import java.sql.SQLException;

public class TicketMessageReceivedEvent implements IEvent<MessageReceivedEvent> {

    private static TicketMessageReceivedEvent instance;

    public static TicketMessageReceivedEvent getInstance() {
        if (instance == null) {
            instance = new TicketMessageReceivedEvent();
        }
        return instance;
    }

    @Override
    public Class<? extends Event> getListenerType() {
        return MessageReceivedEvent.class;
    }

    @Override
    public <T extends Event> void execute(T event) throws SQLException, IOException {
        MessageReceivedEvent castEvent = (MessageReceivedEvent) event;
        if (castEvent.getChannel() instanceof TextChannel
                && TicketService.getInstance().isTicketchannel(castEvent.getChannel().asTextChannel())) {
            Ticket ticket = TicketRepository.getTicketByChannelId(castEvent.getChannel().getIdLong());

            if (!castEvent.getAuthor().getId().equals(ticket.getOwner().getId())) {
                ticket.addParticipant(castEvent.getMember());
            }

            // ticket claim function
            if (Util.isTeamMember(castEvent.getMember())) {
                ticket.claim(castEvent.getMember());
            }

            // ticket owner activity
            if (ticket.isOwner(castEvent.getMember())) {
                TicketRepository.updateTicketLastActivityAt(ticket);
            }
        }
    }
}
