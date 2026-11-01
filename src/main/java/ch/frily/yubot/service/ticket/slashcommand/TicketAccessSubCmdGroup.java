package ch.frily.yubot.service.ticket.slashcommand;

import ch.frily.yubot.interaction.command.ISlashSubcommand;

import java.util.List;

public class TicketAccessSubCmdGroup implements ISlashSubcommand {
    @Override
    public String getName() {
        return "access";
    }

    @Override
    public String getDescription() {
        return "Bearbeite welche Personsn oder Rollen auf das Ticket zugreifen können";
    }

    @Override
    public List<ISlashSubcommand> getSubcommands() {
        return List.of(
                new TicketAccessUserCmd(),
                new TicketAccessTeamCmd()
        );
    }
}
