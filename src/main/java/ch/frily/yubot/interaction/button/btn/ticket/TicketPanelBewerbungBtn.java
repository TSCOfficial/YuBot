package ch.frily.yubot.interaction.button.btn.ticket;

import ch.frily.yubot.feature.ticket.TicketTypeGroup;
import ch.frily.yubot.interaction.button.Button;
import ch.frily.yubot.interaction.modal.modal.TypeSelectorModal;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.jetbrains.annotations.NotNull;

public class TicketPanelBewerbungBtn extends Button {

    private static TicketPanelBewerbungBtn instance;

    public static TicketPanelBewerbungBtn getInstance(){
        if (instance == null) {
            instance = new TicketPanelBewerbungBtn();
        }
        return instance;
    }

    @Override
    public String getId() {
        return "ticket-bewerbung-btn";
    }

    @Override
    public String getLabel() {
        return "Bewerbung";
    }

    @Override
    public ButtonStyle getStyle() {
        return ButtonStyle.PRIMARY;
    }

    @Override
    public void execute(@NotNull ButtonInteractionEvent event) {
        TypeSelectorModal modal = new TypeSelectorModal();
        modal.setTypeGroup(TicketTypeGroup.BEWERBUNG);
        event.replyModal(modal.build()).queue();
    }
}
