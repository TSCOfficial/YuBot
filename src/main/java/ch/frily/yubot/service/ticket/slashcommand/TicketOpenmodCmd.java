package ch.frily.yubot.service.ticket.slashcommand;

import ch.frily.yubot.exception.ThrowingConsumer;
import ch.frily.yubot.service.ticket.Ticket;
import ch.frily.yubot.service.ticket.TicketService;
import ch.frily.yubot.database.repository.TicketRepository;
import ch.frily.yubot.service.ticket.TicketType;
import ch.frily.yubot.interaction.command.ISlashSubcommand;
import ch.frily.yubot.service.ticket.modal.ModTicketSelectorModal;
import ch.frily.yubot.util.EnvKey;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class TicketOpenmodCmd implements ISlashSubcommand {
    @Override
    public String getName() {
        return "openmod";
    }

    @Override
    public String getDescription() {
        return "Erstelle ein Mod-Ticket";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.USER, "user", "Für wen das Modticket geöffnet werden soll.", true)
        );
    }

    @Override
    public List<Role> getAllowedRoles() {
        return Stream.of(
                EnvKey.ROLE_SUPPORT,
                EnvKey.ROLE_MODERATOR
        ).map(EnvKey::getAsRole).toList();
    }

    @Override
    public void execute(@NotNull SlashCommandInteractionEvent event) throws SQLException {
        OptionMapping memberOption = Objects.requireNonNull(event.getOption("user"));

        ModTicketSelectorModal modal = new ModTicketSelectorModal();
        modal.addArgument("target", memberOption.getAsMember().getId());
        modal.setInitiator(event.getMember());
        event.replyModal(modal.build()).queue();
    }
}
