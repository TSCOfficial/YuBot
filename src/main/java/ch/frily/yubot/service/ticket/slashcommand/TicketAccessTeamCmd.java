package ch.frily.yubot.service.ticket.slashcommand;

import ch.frily.yubot.service.ticket.repository.TicketRepository;
import ch.frily.yubot.interaction.command.ISlashSubcommand;
import ch.frily.yubot.service.ticket.Ticket;
import ch.frily.yubot.service.ticket.modal.TicketAccessTeamsModal;
import ch.frily.yubot.util.EnvKey;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.List;

public class TicketAccessTeamCmd implements ISlashSubcommand {
    @Override
    public String getName() {
        return "team";
    }

    @Override
    public String getDescription() {
        return "Bearbeite welche Teams auf das Ticket zugriff haben sollen";
    }

    @Override
    public void execute(@NotNull SlashCommandInteractionEvent event) throws SQLException {
        Ticket ticket = TicketRepository.getTicketByChannelId(event.getChannel().getIdLong());

        TicketAccessTeamsModal modal = new TicketAccessTeamsModal();
        modal.setTicket(ticket);

        event.replyModal(modal.build()).queue();
    }

    @Override
    public List<Role> getAllowedRoles() {
        return List.of(
                EnvKey.ROLE_YUTEAM.getAsRole()
        );
    }
}
