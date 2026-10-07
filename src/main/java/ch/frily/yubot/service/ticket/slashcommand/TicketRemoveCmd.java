package ch.frily.yubot.service.ticket.slashcommand;

import ch.frily.yubot.service.ticket.Ticket;
import ch.frily.yubot.database.repository.TicketRepository;
import ch.frily.yubot.interaction.command.ISlashSubcommand;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.IPermissionHolder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

public class TicketRemoveCmd implements ISlashSubcommand {
    @Override
    public String getName() {
        return "remove";
    }

    @Override
    public String getDescription() {
        return "Entferne eine Person oder Rolle aus dem Ticket";
    }

    @Override
    public void execute(@NotNull SlashCommandInteractionEvent event) throws SQLException {
        OptionMapping userRoleOption = Objects.requireNonNull(event.getOption("user-role"));
        Ticket ticket = TicketRepository.getTicketById(event.getChannelIdLong());
        IMentionable mentionable = userRoleOption.getAsMentionable();
        IPermissionHolder permissionHolder = (IPermissionHolder) mentionable;
        ticket.removeMember(event.getMember(), permissionHolder);

        event.reply(String.format("✅ %s wurde erfolgreich entfernt.", userRoleOption.getAsMentionable().getAsMention())).queue();
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.MENTIONABLE, "user-role", "Person oder Rolle welche vom Ticket entfernt werden soll.", true)
        );
    }
}
