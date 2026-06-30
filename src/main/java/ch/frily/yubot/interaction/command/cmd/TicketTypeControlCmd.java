package ch.frily.yubot.interaction.command.cmd;

import ch.frily.yubot.feature.DynamicMessageList;
import ch.frily.yubot.feature.TicketType;
import ch.frily.yubot.feature.TicketTypeControlRepository;
import ch.frily.yubot.interaction.command.ISlashSubcommand;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jspecify.annotations.NonNull;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
public class TicketTypeControlCmd implements ISlashSubcommand {
    @Override
    public String getName() {
        return "control";
    }

    @Override
    public String getDescription() {
        return "Kontrolliere den Tickettyp";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.STRING, "type", "Der Tickettyp", true)
                        .addChoices(Arrays.stream(TicketType.values()).map(type -> {
                            String choiceName = String.format("%s / %s", type.getGroup().getLabel(), type.getLabel());
                            return new Command.Choice(choiceName, type.name());
                        }).toList()),
                new OptionData(OptionType.INTEGER, "status", "Tickettyp aktivieren", true)
                        .addChoice("Open", 1)
                        .addChoice("Close", 0)
        );
    }

    @Override
    public List<Permission> getDefaultPermissions() {
        return List.of(Permission.ADMINISTRATOR);
    }

    @Override
    public void execute(@NonNull SlashCommandInteractionEvent event) throws SQLException, ClassNotFoundException {
        String typeName = event.getOption("type").getAsString();
        log.debug("status: {}", event.getOption("status").getAsInt());
        boolean isLocked = event.getOption("status").getAsInt() == 0 ? true : false;
        TicketType type = TicketType.valueOf(typeName);
        TicketTypeControlRepository.upsertType(type, isLocked);

        event.reply(String.format("✅ Tickettyp '%s' erfolgreich auf **%s** gesetzt", type.getLabel(), isLocked ? "geschlossen" : "geöffnet")).setEphemeral(true).queue();

        DynamicMessageList.TICKET_PANEL.update(true); // todo save server state to database and get it here
    }
}
