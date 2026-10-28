package ch.frily.yubot.service.ticket.slashcommand;

import ch.frily.yubot.interaction.command.ISlashSubcommand;
import ch.frily.yubot.util.EnvKey;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jspecify.annotations.NonNull;

import java.sql.SQLException;
import java.util.List;

public class TicketSearchParticipantSubCmd implements ISlashSubcommand {
    @Override
    public String getName() {
        return "participant";
    }

    @Override
    public String getDescription() {
        return "Suche nach Tickets in welchen die Person teilgenommen hat";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.USER, "user", "Die Person welche im Ticket teilgenommen hat", true)
        );
    }

    @Override
    public List<Role> getAllowedRoles() {
        return List.of(
                EnvKey.ROLE_YUTEAM.getAsRole()
        );
    }

    @Override
    public void execute(@NonNull SlashCommandInteractionEvent event) throws SQLException, ClassNotFoundException {
        ISlashSubcommand.super.execute(event);
    }
}
