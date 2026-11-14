package ch.frily.yubot.service.ticket.slashcommand;

import ch.frily.yubot.container.ContainerContext;
import ch.frily.yubot.service.ticket.container.TicketSearchContainer;
import ch.frily.yubot.interaction.command.ISlashSubcommand;
import ch.frily.yubot.EnvKey;
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

public class TicketSearchNameSubCmd implements ISlashSubcommand {
    @Override
    public String getName() {
        return "name";
    }

    @Override
    public String getDescription() {
        return "Suche nach Tickets welche den Suchbegriff im Namen enthalten";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.STRING, "text", "Der Suchbegriff", true)
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
        OptionMapping searchQueryMapping = Objects.requireNonNull(event.getOption("text"));
        String searchQuery = searchQueryMapping.getAsString();

        Map<String, String> ctxArguments = Map.of(
                "q", searchQuery.toLowerCase(),
                "search", TicketSearchSubCmdGroup.SearchType.NAME.name()
        );

        ContainerContext ctx = new ContainerContext(0, event, ctxArguments);
        TicketSearchContainer container = new TicketSearchContainer(ctx);
        event.getHook().editOriginalComponents(container.build()).setAllowedMentions(List.of()).useComponentsV2().queue();
    }
}
