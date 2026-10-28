package ch.frily.yubot.service.ticket.slashcommand;

import ch.frily.yubot.container.ticket.TicketSearchContainer;
import ch.frily.yubot.database.repository.TicketRepository;
import ch.frily.yubot.interaction.command.ISlashSubcommand;
import ch.frily.yubot.service.ticket.Ticket;
import ch.frily.yubot.util.EnvKey;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jspecify.annotations.NonNull;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

public class TicketSearchOwnerSubCmd implements ISlashSubcommand {
    @Override
    public String getName() {
        return "owner";
    }

    @Override
    public String getDescription() {
        return "Suche nach Tickets welche einer Person gehörten";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.USER, "user", "Die Person welche das Tickets besass", true)
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
        OptionMapping ownerMapping = Objects.requireNonNull(event.getOption("user"));
        User userOwner = ownerMapping.getAsUser();
        Member memberOwner = ownerMapping.getAsMember();
        List<Ticket> ownedTickets = TicketRepository.getTicketsByUser(userOwner);

        TicketSearchContainer container = new TicketSearchContainer(ownedTickets, memberOwner, true);
        event.replyComponents(container.build()).useComponentsV2().queue();
    }
}
