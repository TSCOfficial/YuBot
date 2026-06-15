package ch.frily.yubot.interaction.button.btn;

import ch.frily.yubot.interaction.button.IButton;
import ch.frily.yubot.feature.TicketTypeGroup;
import ch.frily.yubot.interaction.modal.modal.TypeSelectorModal;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.jetbrains.annotations.NotNull;

public class TicketPanelSupportBtn implements IButton {

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
        return ButtonStyle.PRIMARY;
    }

    @Override
    public void execute(@NotNull ButtonInteractionEvent event) {
        TypeSelectorModal modal = new TypeSelectorModal();
        modal.setTypeGroup(TicketTypeGroup.SUPPORT);
        event.replyModal(modal.build()).queue();
    }
}
