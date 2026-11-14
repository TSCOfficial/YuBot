package ch.frily.yubot.service.ticket.slashcommand;

import ch.frily.yubot.container.ContainerContext;
import ch.frily.yubot.container.ticket.TicketSearchContainer;
import ch.frily.yubot.interaction.command.ISlashSubcommand;
import ch.frily.yubot.EnvKey;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jspecify.annotations.NonNull;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
        event.deferReply(true).useComponentsV2().queue();
        OptionMapping participantMapping = Objects.requireNonNull(event.getOption("user"));
        Member memberParticipant = participantMapping.getAsMember();

        Map<String, String> ctxArguments = Map.of(
                "target", memberParticipant.getId(),
                "search", TicketSearchSubCmdGroup.SearchType.PARTICIPANT.name()
        );

        ContainerContext ctx = new ContainerContext(0, event, ctxArguments);
        TicketSearchContainer container = new TicketSearchContainer(ctx);
        event.getHook().editOriginalComponents(container.build()).setAllowedMentions(List.of()).useComponentsV2().queue();
    }
}
