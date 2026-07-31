package ch.frily.yubot.interaction.button.btn;

import ch.frily.yubot.interaction.button.IButton;
import ch.frily.yubot.feature.TicketTypeGroup;
import ch.frily.yubot.interaction.modal.modal.TypeSelectorModal;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.jetbrains.annotations.NotNull;

public class TicketPanelAwarenessBtn implements IButton {

    private static TicketPanelAwarenessBtn instance;

    public static TicketPanelAwarenessBtn getInstance(){
        if (instance == null) {
            instance = new TicketPanelAwarenessBtn();
        }
        return instance;
    }

    @Override
    public String defineId() {
        return "ticket-awareness";
    }

    @Override
    public String getLabel() {
        return "Awareness";
    }

    @Override
    public ButtonStyle getStyle() {
        return ButtonStyle.SUCCESS;
    }

    @Override
    public void execute(@NotNull ButtonInteractionEvent event) {
        TypeSelectorModal modal = new TypeSelectorModal();
        modal.setTypeGroup(TicketTypeGroup.AWARENESS);
        event.replyModal(modal.build()).queue();
    }
}
