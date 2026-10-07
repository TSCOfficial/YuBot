package ch.frily.yubot.service.ticket.scheduler;

import ch.frily.yubot.exception.ExceptionHandler;
import ch.frily.yubot.service.ticket.Ticket;
import ch.frily.yubot.database.repository.TicketRepository;
import ch.frily.yubot.service.ticket.TicketStatus;
import ch.frily.yubot.scheduler.IScheduler;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Checks the activity of the ticket and acts accordingly
 * @since 1.4.3
 * @author aliz frily
 */
@Slf4j
public class TicketActivityScheduler implements IScheduler {
    @Override
    public void execute() throws SQLException {
        List<Ticket> outdatedTickets = TicketRepository.getTickets().stream().filter(ticket -> {
            if (ticket.getStatus() != TicketStatus.NEW) {
                return false;
            }
            if (ticket.getLastActivityAt().plusHours(Ticket.getMAX_OWNER_NOREPLY_DURATION()).isAfter(LocalDateTime.now())) { // time needs to be before now (in the past)
                return false;
            }
            // todo add new flag "owner has already written", to check if the ticket-opener ignosres the ticket or not. return true if the owner DID NOT already write something
            if (ticket.isReminderSent()) {
                return false;
            }
            return true;
        }).toList();
        outdatedTickets.forEach(ticket -> {
            try {
                ticket.sendReminder();
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        });
    }

    @Override
    public String cronExpression() {
        return "0 * * * *";
    }
}
