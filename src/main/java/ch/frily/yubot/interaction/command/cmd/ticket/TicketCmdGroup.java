package ch.frily.yubot.interaction.command.cmd.ticket;

import ch.frily.yubot.interaction.command.ISlashCommandGroup;
import ch.frily.yubot.interaction.command.ISlashSubcommand;

import java.util.List;

public class TicketCmdGroup implements ISlashCommandGroup {
    @Override
    public String getName() {
        return "ticket";
    }

    @Override
    public List<ISlashSubcommand> getSubcommands() {
        return List.of(
                new TicketCloseCmd(),
                new TicketAddCmd(),
                new TicketRemoveCmd(),
                new TicketTypeControlCmd(),
                new TicketOpenmodCmd(),
                new TicketReopenCmd()
        );
    }
}
