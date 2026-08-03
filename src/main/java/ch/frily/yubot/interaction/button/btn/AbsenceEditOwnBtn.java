package ch.frily.yubot.interaction.button.btn;

import ch.frily.yubot.container.AbsenceEditOwnContainer;
import ch.frily.yubot.interaction.button.Button;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.jspecify.annotations.NonNull;

import java.sql.SQLException;

public class AbsenceEditOwnBtn extends Button {
    @Override
    public String getId() {
        return "absence-edit-own-btn";
    }

    @Override
    public String getLabel() {
        return "Eigene Absenzen bearbeiten";
    }

    @Override
    public ButtonStyle getStyle() {
        return ButtonStyle.SECONDARY;
    }

    @Override
    public void execute(@NonNull ButtonInteractionEvent event) throws SQLException, ClassNotFoundException, NoSuchMethodException {
        event.replyComponents(new AbsenceEditOwnContainer(event.getMember()).build()).useComponentsV2().setEphemeral(true).queue();
    }
}
