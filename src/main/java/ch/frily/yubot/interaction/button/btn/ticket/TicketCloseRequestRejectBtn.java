package ch.frily.yubot.interaction.button.btn.ticket;

import ch.frily.yubot.embed.ticket.TicketCloseRejectedEmbed;
import ch.frily.yubot.feature.ticket.Ticket;
import ch.frily.yubot.database.repository.TicketRepository;
import ch.frily.yubot.interaction.button.Button;
import ch.frily.yubot.util.EnvResolver;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class TicketCloseRequestRejectBtn extends Button {

    @Setter
    private boolean disabled = false;

    @Override
    public String getId() {
        return "ticket-close-request-reject-btn";
    }

    @Override
    public String getLabel() {
        return "Nein, abbrechen";
    }

    @Override
    public ButtonStyle getStyle() {
        return ButtonStyle.SECONDARY;
    }

    @Override
    public boolean isDisabled() {
        return disabled;
    }

    @Override
    public void execute(@NotNull ButtonInteractionEvent event) throws SQLException, ClassNotFoundException {

        Ticket ticket = TicketRepository.getTicketById(event.getChannelIdLong());
        ticket.rejectCloseRequest(event);

        TicketCloseRejectedEmbed embed = new TicketCloseRejectedEmbed();
        embed.setMember(event.getMember());
        embed.setTicket(ticket);

        event.editMessageEmbeds(embed.build())
                .setComponents(event.getMessage().getComponentTree().asDisabled())
                .queue();

        CompletableFuture<Message> welcomeMessage = EnvResolver.getMessageById(event.getGuild().getIdLong(), ticket.getChannel().getIdLong(), ticket.getWelcomeMessageId());
        welcomeMessage.thenAccept(message -> {
          message.editMessageComponents(message.getComponentTree().asEnabled()).queue();
        });


    }
}
