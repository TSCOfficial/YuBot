package ch.frily.yubot.interaction.button.btn.ticket;

import ch.frily.yubot.feature.ticket.TicketManager;
import ch.frily.yubot.feature.ticket.TicketType;
import ch.frily.yubot.interaction.button.Button;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.jspecify.annotations.NonNull;

import java.sql.SQLException;

/**
 * This button is used to confirm the opening a new ticket, when the user already has an opened ticket with the same {@link TicketType}.
 */
public class TicketConfirmOpenBtn extends Button {

    @Override
    public String getId() {
        return "ticket-confirm-open";
    }

    @Override
    public String getLabel() {
        return "Ja, Ticket öffnen";
    }

    @Override
    public ButtonStyle getStyle() {
        return ButtonStyle.PRIMARY;
    }

    @Override
    public void execute(@NonNull ButtonInteractionEvent event) throws SQLException, ClassNotFoundException, NoSuchMethodException {
        String ticketTypeString = getArgument(event.getComponentId(), "type");
        TicketType ticketType = TicketType.valueOf(ticketTypeString);

        TicketManager.getInstance().createTicket(ticketType, event.getMember(), channel -> {
            event.editMessage("Dein Ticket wurde erstellt: " + channel.getAsMention()).setComponents(event.getMessage().getComponentTree().asDisabled()).queue();
        });
    }
}
