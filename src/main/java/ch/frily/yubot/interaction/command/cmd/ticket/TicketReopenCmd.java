package ch.frily.yubot.interaction.command.cmd.ticket;

import ch.frily.yubot.feature.ticket.Ticket;
import ch.frily.yubot.database.repository.TicketRepository;
import ch.frily.yubot.interaction.command.ISlashSubcommand;
import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jspecify.annotations.NonNull;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Stream;

public class TicketReopenCmd implements ISlashSubcommand {
    @Override
    public String getName() {
        return "reopen";
    }

    @Override
    public String getDescription() {
        return "Öffne das ticket erneut, nachdem es geschlossen wurde";
    }

    @Override
    public void execute(@NonNull SlashCommandInteractionEvent event) throws SQLException, ClassNotFoundException {
        Ticket ticket = TicketRepository.getTicketById(event.getChannelIdLong());

        ticket.reopen(event.getMember(), event);
    }

    @Override
    public List<Role> getAllowedRoles() {
        return Stream.of(
                EnvKey.ROLE_YUTEAM
        ).map(EnvResolver::getRoleById).toList();
    }
}
