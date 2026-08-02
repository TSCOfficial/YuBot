package ch.frily.yubot.interaction.button.btn;

import ch.frily.yubot.embed.TicketCloseAcceptedEmbed;
import ch.frily.yubot.embed.TicketClosedOptionsEmbed;
import ch.frily.yubot.feature.Ticket;
import ch.frily.yubot.feature.TicketRepository;
import ch.frily.yubot.interaction.button.Button;
import ch.frily.yubot.interaction.button.IButton;
import ch.frily.yubot.util.Util;
import javassist.NotFoundException;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.List;

@Slf4j
public class TicketCloseRequestAcceptBtn extends Button {

    @Setter
    private boolean disabled = false;

    @Override
    public String defineId() {
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
