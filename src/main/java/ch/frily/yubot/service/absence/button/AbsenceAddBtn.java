package ch.frily.yubot.service.absence.button;

import ch.frily.yubot.interaction.button.Button;
import ch.frily.yubot.service.absence.modal.AbsenceAddModal;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.jspecify.annotations.NonNull;

public class AbsenceAddBtn extends Button {

    @Override
    public String getId() {
        return "add-absence-btn";
    }

    @Override
    public String getLabel() {
        return "Abwesenheit anlegen";
    }

    @Override
    public ButtonStyle getStyle() {
        return ButtonStyle.PRIMARY;
    }

    @Override
    public void execute(@NonNull ButtonInteractionEvent event) {
        AbsenceAddModal addAbence = new AbsenceAddModal();
        addAbence.setMember(event.getMember());
        event.replyModal(addAbence.build()).queue();
    }
}
