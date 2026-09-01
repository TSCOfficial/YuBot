package ch.frily.yubot.interaction.button.btn.ticket;

import ch.frily.yubot.embed.ticket.TicketCloseAcceptedEmbed;
import ch.frily.yubot.feature.ticket.Ticket;
import ch.frily.yubot.database.repository.TicketRepository;
import ch.frily.yubot.interaction.button.Button;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

@Slf4j
public class TicketCloseRequestAcceptBtn extends Button {

    @Setter
    private boolean disabled = false;

    @Override
    public String getId() {
        return "ticket-close-request-accept-btn";
    }

    @Override
    public String getLabel() {
        return "Ja, schliessen";
    }

    @Override
    public ButtonStyle getStyle() {
        return ButtonStyle.PRIMARY;
    }

    @Override
    public boolean isDisabled() {
        return disabled;
    }

    @Override
    public void execute(@NotNull ButtonInteractionEvent event) throws SQLException, ClassNotFoundException {
        Ticket ticket = TicketRepository.getTicketById(event.getChannelIdLong());
        ticket.acceptCloseRequest(event);

        TicketCloseAcceptedEmbed acceptedEmbed = new TicketCloseAcceptedEmbed();
        acceptedEmbed.setMember(event.getMember());
        acceptedEmbed.setTicket(ticket);

        event.getMessage().editMessageEmbeds(acceptedEmbed.build())
                .setComponents(event.getMessage().getComponentTree().asDisabled())
                .queue();
    }
}
