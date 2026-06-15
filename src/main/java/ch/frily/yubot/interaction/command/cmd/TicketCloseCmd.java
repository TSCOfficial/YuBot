package ch.frily.yubot.interaction.command.cmd;

import ch.frily.yubot.embed.TicketCloseRequestEmbed;
import ch.frily.yubot.embed.TicketClosedOptionsEmbed;
import ch.frily.yubot.feature.Ticket;
import ch.frily.yubot.feature.TicketRepository;
import ch.frily.yubot.interaction.button.btn.TicketCloseRequestAcceptBtn;
import ch.frily.yubot.interaction.button.btn.TicketCloseRequestRejectBtn;
import ch.frily.yubot.interaction.button.btn.TicketDeleteBtn;
import ch.frily.yubot.interaction.command.ISlashSubcommand;
import ch.frily.yubot.util.EnvResolver;
import javassist.NotFoundException;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
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
    public void execute(@NotNull SlashCommandInteractionEvent event) {
        try {
            Ticket ticket = TicketRepository.getTicketById(event.getChannelIdLong());

            if (event.getOption("force") != null && event.getOption("force").getAsBoolean()) {
                if (ticket.isForceClosable()) {
                    ActionRow actionRow = ActionRow.of(
                            new TicketDeleteBtn().build()
                    );
                    ticket.close(event.getMember());

                    TicketClosedOptionsEmbed optionsEmbed = new TicketClosedOptionsEmbed();
                    optionsEmbed.setTicket(ticket);
                    optionsEmbed.setForcedClosed(true);
                    event.replyEmbeds(optionsEmbed.build()).setComponents(actionRow).queue();
                    return;
                }
                event.reply("❌ Ticket kann nicht geschlossen werden.\n-# Es müssen erst min. 2 Anfragen gestellt werden oder 7 Tage inaktivität.").setEphemeral(true).queue();
                return;
            } else {
                ActionRow actionRow = ActionRow.of(
                        new TicketCloseRequestAcceptBtn().build(),
                        new TicketCloseRequestRejectBtn().build()
                );

                ticket.requestClose(event.getMember());

                TicketCloseRequestEmbed requestEmbed = new TicketCloseRequestEmbed();
                requestEmbed.setInitiator(event.getMember());
                requestEmbed.setTicket(ticket);
                event.replyEmbeds(requestEmbed.build()).setComponents(actionRow).queue();

                // Disable welcome message component
                EnvResolver.getMessageById(event.getGuild().getIdLong(), ticket.getChannel().getIdLong(), ticket.getWelcomeMessageId()).thenAccept(message -> {
                    message.editMessageComponents(message.getComponentTree().asDisabled()).queue();
                });
            }

        } catch (SQLException | NotFoundException | IllegalStateException | PermissionException exception) {
            event.reply(exception.getMessage()).queue();
        }
    }
}
