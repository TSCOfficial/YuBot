package ch.frily.yubot.service.ticket.schedule;

import ch.frily.yubot.exception.ExceptionHandler;
import ch.frily.yubot.service.ticket.Ticket;
import ch.frily.yubot.database.repository.TicketRepository;
import ch.frily.yubot.service.ticket.TicketService;
import ch.frily.yubot.service.ticket.TicketStatus;
import ch.frily.yubot.scheduler.IScheduler;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.List;
import java.util.Objects;

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
            if (ticket.getCreatedAt().truncatedTo(ChronoUnit.SECONDS).plusHours(Ticket.getMAX_OWNER_NOREPLY_DURATION()).isAfter(LocalDateTime.now())) {
                return false;
            }
            if (Objects.nonNull(ticket.getLastActivityAt())) { // discards tickets in which someone already wrote something
                return false;
            }
            return !ticket.isReminderSent();
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
