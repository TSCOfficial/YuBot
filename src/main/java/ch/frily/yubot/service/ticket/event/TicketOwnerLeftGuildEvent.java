package ch.frily.yubot.service.ticket.event;

import ch.frily.yubot.database.repository.TicketRepository;
import ch.frily.yubot.event.IEvent;
import ch.frily.yubot.exception.ExceptionHandler;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;

import java.io.IOException;
import java.sql.SQLException;

public class TicketOwnerLeftGuildEvent implements IEvent<GuildMemberRemoveEvent> {

    private static TicketOwnerLeftGuildEvent instance;

    public static TicketOwnerLeftGuildEvent getInstance() {
        if (instance == null) {
            instance = new TicketOwnerLeftGuildEvent();
        }
        return instance;
    }

    @Override
    public Class<? extends Event> getListenerType() {
        return GuildMemberRemoveEvent.class;
    }

    @Override
    public <T extends Event> void execute(T event) throws SQLException, IOException {
        GuildMemberRemoveEvent castEvent = (GuildMemberRemoveEvent) event;
        if (castEvent.getMember() != null) {
            TicketRepository.getTicketsByUser(castEvent.getMember().getUser()).forEach(ticket -> {
                try {
                    ticket.getChannel().sendMessage("Der Benutzer hat den Server verlassen. Das Ticket wird geschlossen.").queue();
                    ticket.close(null, true);
                } catch (Exception exception) {
                    ExceptionHandler.handle(exception);
                }
            });
        }
    }
}
