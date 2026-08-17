package ch.frily.yubot.scheduler;

import ch.frily.yubot.exception.ExceptionHandler;
import ch.frily.yubot.feature.Ticket;
import ch.frily.yubot.feature.TicketRepository;
import ch.frily.yubot.feature.TicketStatus;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * Checks the activity of the ticket and acts accordingly
 * @since 1.4.3
 * @author aliz frily
 */
@Slf4j
public class TicketActivityScheduler implements IScheduler{
    @Override
    public void execute() throws SQLException, ClassNotFoundException {
        List<Ticket> outdatedTickets = TicketRepository.getTickets().stream().filter(ticket -> {
            if (ticket.getStatus() != TicketStatus.NEW) {
                return false;
            }
            if (ticket.getLastActivityAt().plusHours(Ticket.getMAX_OWNER_NOREPLY_DURATION()).isAfter(LocalDateTime.now())) { // time needs to be before now (in the past)
                return false;
            }
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
