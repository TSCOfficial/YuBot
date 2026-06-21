package ch.frily.yubot.interaction.command.cmd;

import ch.frily.yubot.feature.Ticket;
import ch.frily.yubot.feature.TicketRepository;
import ch.frily.yubot.interaction.command.ISlashSubcommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.List;

public class TicketAddCmd implements ISlashSubcommand {
    @Override
    public String getName() {
        return "add";
    }

    @Override
    public String getDescription() {
        return "Füge eine Person zum Ticket hinzu";
    }

    @Override
    public void execute(@NotNull SlashCommandInteractionEvent event) throws SQLException {
        Ticket ticket = TicketRepository.getTicketById(event.getChannelIdLong());
        ticket.addMember(event.getMember(), event.getOption("user").getAsMember());

        event.reply(String.format("✅ %s wurde erfolgreich hinzugefügt.", event.getOption("user").getAsMember().getAsMention())).queue();
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.USER, "user", "Person welche zum Ticket hinzugefügt werden soll.", true)
        );
    }

    @Override
    public List<Permission> getDefaultPermissions() {
        return ISlashSubcommand.super.getDefaultPermissions();
    }
}
