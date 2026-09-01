package ch.frily.yubot.interaction.command.cmd.ticket;

import ch.frily.yubot.exception.ThrowingConsumer;
import ch.frily.yubot.feature.ticket.Ticket;
import ch.frily.yubot.feature.ticket.TicketManager;
import ch.frily.yubot.database.repository.TicketRepository;
import ch.frily.yubot.feature.ticket.TicketType;
import ch.frily.yubot.interaction.command.ISlashSubcommand;
import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.List;
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
        ).map(EnvResolver::getRoleById).toList();
    }

    @Override
    public void execute(@NotNull SlashCommandInteractionEvent event) throws SQLException, ClassNotFoundException {
        event.deferReply(true).queue();
        Member member = event.getOption("user").getAsMember();
        TicketManager.getInstance().createTicket(TicketType.MODTICKET, member, ThrowingConsumer.wrap(event, channel -> {
            event.getHook().editOriginal(String.format("Modticket wurde erstellt: %s", channel.getAsMention())).queue();

            Ticket ticket = TicketRepository.getTicketById(channel.getIdLong());
            ticket.claim(event.getMember());
        }));
    }
}
