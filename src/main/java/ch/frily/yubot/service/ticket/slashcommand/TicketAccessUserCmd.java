package ch.frily.yubot.service.ticket.slashcommand;

import ch.frily.yubot.database.repository.TicketRepository;
import ch.frily.yubot.interaction.command.ISlashSubcommand;
import ch.frily.yubot.service.ticket.Ticket;
import ch.frily.yubot.service.ticket.modal.TicketAddUsersModal;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

public class TicketAccessUserCmd implements ISlashSubcommand {
    @Override
    public String getName() {
        return "user";
    }

    @Override
    public String getDescription() {
        return "Bearbeite welche Personen zugriff auf das Ticket haben sollen.";
    }

    @Override
    public void execute(@NotNull SlashCommandInteractionEvent event) throws SQLException {
        Ticket ticket = TicketRepository.getTicketByChannelId(event.getChannel().getIdLong());
        TicketAddUsersModal modal =  new TicketAddUsersModal();
        modal.setTicket(ticket);

        event.replyModal(modal.build()).queue();
    }
}
