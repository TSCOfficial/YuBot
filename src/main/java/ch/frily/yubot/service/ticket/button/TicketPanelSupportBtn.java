package ch.frily.yubot.service.ticket.button;

import ch.frily.yubot.interaction.button.Button;
import ch.frily.yubot.service.ticket.TicketTypeGroup;
import ch.frily.yubot.service.ticket.modal.TicketTypeSelectorModal;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.jetbrains.annotations.NotNull;

public class TicketPanelSupportBtn extends Button {

    private static TicketPanelSupportBtn instance;

    public static TicketPanelSupportBtn getInstance(){
        if (instance == null) {
            instance = new TicketPanelSupportBtn();
        }
        return instance;
    }

    @Override
    public String getId() {
        return "ticket-support";
    }

    @Override
    public String getLabel() {
        return "Support";
    }

    @Override
    public ButtonStyle getStyle() {
        return ButtonStyle.SECONDARY;
    }

    @Override
    public void execute(@NotNull ButtonInteractionEvent event) {
        TicketTypeSelectorModal modal = new TicketTypeSelectorModal();
        modal.setTypeGroup(TicketTypeGroup.SUPPORT);
        event.replyModal(modal.build()).queue();
    }
}
