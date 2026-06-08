package ch.frily.yubot.interaction.button.btn;

import ch.frily.yubot.interaction.button.IButton;
import ch.frily.yubot.feature.TicketTypeGroup;
import ch.frily.yubot.interaction.modal.modal.TypeSelectorModal;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.jetbrains.annotations.NotNull;

public class SupportButton implements IButton {

    private static SupportButton instance;

    public static SupportButton getInstance(){
        if (instance == null) {
            instance = new SupportButton();
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
