package ch.frily.yubot.interaction.button.btn.profile;

import ch.frily.yubot.interaction.button.Button;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.jspecify.annotations.NonNull;

import java.sql.SQLException;

public class UseProfileBtn extends Button {
    @Override
    public String getId() {
        return "use-profile-btn";
    }

    @Override
    public String getLabel() {
        return "Profil anwenden";
    }

    @Override
    public ButtonStyle getStyle() {
        return ButtonStyle.PRIMARY;
    }

    @Override
    public void execute(@NonNull ButtonInteractionEvent event) throws SQLException, ClassNotFoundException, NoSuchMethodException {

    }
}
