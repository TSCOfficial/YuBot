package ch.frily.yubot.feature.ticket.button;

import ch.frily.yubot.interaction.button.Button;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.jspecify.annotations.NonNull;

/**
 * This button is the opposite of {@link TicketConfirmOpenBtn}. The user can cancel the ticket creation process.
 * */
public class TicketCancelOpenBtn extends Button {
    @Override
    public String getId() {
        return "ticket-cancel-optn";
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
    public void execute(@NonNull ButtonInteractionEvent event) {
        event.editMessage("Ticket erstellen wurde abgebrochen.").setComponents(event.getMessage().getComponentTree().asDisabled()).queue();
    }
}
