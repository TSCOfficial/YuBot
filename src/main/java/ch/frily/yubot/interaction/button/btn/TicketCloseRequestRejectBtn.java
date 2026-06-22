package ch.frily.yubot.interaction.button.btn;

import ch.frily.yubot.embed.TicketCloseRejectedEmbed;
import ch.frily.yubot.feature.Ticket;
import ch.frily.yubot.feature.TicketRepository;
import ch.frily.yubot.interaction.button.IButton;
import ch.frily.yubot.util.EnvResolver;
import ch.frily.yubot.util.Util;
import javassist.NotFoundException;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class TicketCloseRequestRejectBtn implements IButton {

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
        ticket.rejectCloseRequest(event.getMember());

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
