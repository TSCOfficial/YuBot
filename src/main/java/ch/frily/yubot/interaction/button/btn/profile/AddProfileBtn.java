package ch.frily.yubot.interaction.button.btn.profile;

import ch.frily.yubot.interaction.button.Button;
import ch.frily.yubot.interaction.modal.modal.AddProfileModal;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.jspecify.annotations.NonNull;

import java.sql.SQLException;

public class AddProfileBtn extends Button {

    @Override
    public String getId() {
        return "add-profile-btn";
    }

    @Override
    public String getLabel() {
        return "Profil hinzufügen";
    }

    @Override
    public ButtonStyle getStyle() {
        return ButtonStyle.SECONDARY;
    }

    @Override
    public void execute(@NonNull ButtonInteractionEvent event) throws SQLException, ClassNotFoundException, NoSuchMethodException {
        event.replyModal(new AddProfileModal().build()).queue();
    }
}
