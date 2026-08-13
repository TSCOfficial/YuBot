package ch.frily.yubot.scheduler;

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
                log.info(String.valueOf(ticket.getChannel().getTimeCreated().atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime()));
                log.info(String.valueOf(ticket.getLastActivityAt()));
                log.info("Would have been sen, but reminder already sent for ticket {}", ticket.getId());
                return false;
            }
            return true;
        }).toList();
        log.info("Found {} outdated tickets", outdatedTickets.size());
        outdatedTickets.forEach(Ticket::sendReminder);
    }

    @Override
    public String cronExpression() {
        return "0 * * * *";
    }
}
