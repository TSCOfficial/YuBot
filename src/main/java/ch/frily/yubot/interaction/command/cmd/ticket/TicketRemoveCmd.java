package ch.frily.yubot.interaction.command.cmd.ticket;

import ch.frily.yubot.feature.ticket.Ticket;
import ch.frily.yubot.feature.ticket.TicketRepository;
import ch.frily.yubot.interaction.command.ISlashSubcommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.List;

public class TicketRemoveCmd implements ISlashSubcommand {
    @Override
    public String getName() {
        return "remove";
    }

    @Override
    public String getDescription() {
        return "Entferne eine Person aus dem Ticket";
    }

    @Override
    public void execute(@NotNull SlashCommandInteractionEvent event) throws SQLException, ClassNotFoundException {
        Ticket ticket = TicketRepository.getTicketById(event.getChannelIdLong());
        ticket.removeMember(event.getMember(), event.getOption("user").getAsMember());

        event.reply(String.format("✅ %s wurde erfolgreich entfernt.", event.getOption("user").getAsMember().getAsMention())).queue();
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.USER, "user", "Person welche vom Ticket entfernt werden soll.", true)
        );
    }

    @Override
    public List<Permission> getDefaultPermissions() {
        return ISlashSubcommand.super.getDefaultPermissions();
    }
}
