package ch.frily.yubot.feature.ticket.slashcommand;

import ch.frily.yubot.feature.ticket.Ticket;
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

public class TicketAddCmd implements ISlashSubcommand {
    @Override
    public String getName() {
        return "add";
    }

    @Override
    public String getDescription() {
        return "Füge eine Person oder eine Rolle zum Ticket hinzu";
    }

    @Override
    public void execute(@NotNull SlashCommandInteractionEvent event) throws SQLException {
        OptionMapping userRoleOption = Objects.requireNonNull(event.getOption("user-role"));
        OptionMapping reasonOption = Objects.requireNonNull(event.getOption("reason"));
        IMentionable mentionable = userRoleOption.getAsMentionable();
        IPermissionHolder permissionHolder = (IPermissionHolder) mentionable;

        Ticket ticket = TicketRepository.getTicketById(event.getChannelIdLong());

        ticket.addMember(event.getMember(), permissionHolder);

        StringBuilder reply = new StringBuilder();
        reply.append(String.format("✅ %s wurde erfolgreich hinzugefügt.", userRoleOption.getAsMentionable().getAsMention()));
        if (event.getOption("reason") != null) {
            reply.append(String.format("\n-# Begründung: %s", reasonOption.getAsString()));
        }

        event.reply(reply.toString()).queue();
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.MENTIONABLE, "user-role", "Person oder Rolle welche zum Ticket hinzugefügt werden soll.", true),
                new OptionData(OptionType.STRING, "reason", "Weshalb wird die Person/Rolle hinzugefügt?")
        );
    }
}
