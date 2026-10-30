package ch.frily.yubot.service.ticket.slashcommand;

import ch.frily.yubot.interaction.command.ISlashSubcommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

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

    }
}
