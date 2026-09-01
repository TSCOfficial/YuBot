package ch.frily.yubot.interaction.command.cmd.ticket;

import ch.frily.yubot.feature.dynamicmsg.DynamicMessageList;
import ch.frily.yubot.feature.ticket.TicketType;
import ch.frily.yubot.database.repository.TicketTypeControlRepository;
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
        List<Command.Choice> choices = Arrays.stream(TicketType.values())
                .filter(type -> type.getGroup() != null)
                .map(type -> {
                    String status = "";
                    try {
                        status = TicketTypeControlRepository.isTypeLocked(type) ? " (🔒)" : " (🔓)";
                    } catch (SQLException | ClassNotFoundException e) {
                        log.error("Failed to get ticket type", e);
                    }
                    String choiceName = String.format("%s / %s %s", type.getGroup().getLabel(), type.getLabel(), status);
                    return new Command.Choice(choiceName, type.name());
                }).toList();

        return List.of(
                new OptionData(OptionType.STRING, "type", "Der Tickettyp", true)
                        .addChoices(choices),
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
        boolean isLocked = event.getOption("status").getAsInt() == 0 ? true : false;

        TicketType type = TicketType.valueOf(typeName);
        TicketTypeControlRepository.upsertType(type, isLocked);

        event.reply(String.format("✅ Tickettyp '%s' erfolgreich auf **%s** gesetzt", type.getLabel(), isLocked ? "geschlossen" : "geöffnet")).setEphemeral(true).queue();

        DynamicMessageList.TICKET_PANEL.update();
        DynamicMessageList.TEAMLIST.update();
    }
}
