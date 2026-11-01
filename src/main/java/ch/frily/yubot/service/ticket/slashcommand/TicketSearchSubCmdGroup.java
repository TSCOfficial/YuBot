package ch.frily.yubot.service.ticket.slashcommand;

import ch.frily.yubot.interaction.command.ISlashSubcommand;
import ch.frily.yubot.util.EnvKey;
import net.dv8tion.jda.api.entities.Role;

import java.util.List;

public class TicketSearchSubCmdGroup implements ISlashSubcommand {

    public enum SearchType {
        OWNER,
        PARTICIPANT,
        NAME
    }

    @Override
    public String getName() {
        return "search";
    }

    @Override
    public String getDescription() {
        return "Suche nach Tickets einer Person";
    }

    @Override
    public List<ISlashSubcommand> getSubcommands() {
        return List.of(
                new TicketSearchOwnerSubCmd(),
                new TicketSearchParticipantSubCmd(),
                new TicketSearchNameSubCmd()
        );
    }
}
