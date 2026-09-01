package ch.frily.yubot.interaction.command.cmd.ticket;

import ch.frily.yubot.embed.ticket.TicketCloseRequestEmbed;
import ch.frily.yubot.feature.ticket.Ticket;
import ch.frily.yubot.database.repository.TicketRepository;
import ch.frily.yubot.interaction.button.btn.ticket.TicketCloseRequestAcceptBtn;
import ch.frily.yubot.interaction.button.btn.ticket.TicketCloseRequestRejectBtn;
import ch.frily.yubot.interaction.command.ISlashSubcommand;
import ch.frily.yubot.util.EnvResolver;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.List;

public class TicketCloseCmd implements ISlashSubcommand {
    @Override
    public String getName() {
        return "close";
    }

    @Override
    public String getDescription() {
        return "Erstelle eine Schliessanfrage";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.BOOLEAN, "force", "Ticketschliessung erzwingen")
        );
    }

    @Override
    public void execute(@NotNull SlashCommandInteractionEvent event) throws SQLException, ClassNotFoundException {
        Ticket ticket = TicketRepository.getTicketById(event.getChannelIdLong());

        if (event.getOption("force") != null && event.getOption("force").getAsBoolean()) {
            ticket.forceClose(event);
        } else {
            ActionRow actionRow = ActionRow.of(
                    new TicketCloseRequestAcceptBtn().build(),
                    new TicketCloseRequestRejectBtn().build()
            );

            ticket.requestClose(event);

            TicketCloseRequestEmbed requestEmbed = new TicketCloseRequestEmbed();
            requestEmbed.setInitiator(event.getMember());
            requestEmbed.setTicket(ticket);
            event.replyEmbeds(requestEmbed.build()).setComponents(actionRow).queue();

            // Disable welcome message component
            EnvResolver.getMessageById(event.getGuild().getIdLong(), ticket.getChannel().getIdLong(), ticket.getWelcomeMessageId()).thenAccept(message -> {
                message.editMessageComponents(message.getComponentTree().asDisabled()).queue();
            });
        }
    }
}
