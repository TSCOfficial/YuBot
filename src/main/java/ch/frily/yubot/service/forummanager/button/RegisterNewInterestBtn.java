package ch.frily.yubot.service.forummanager.button;

import ch.frily.yubot.interaction.button.Button;
import ch.frily.yubot.service.forummanager.modal.RegisterNewInterestModal;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.jspecify.annotations.NonNull;

import java.sql.SQLException;

/**
 * The button that allows users to register a new special interest
 * @since v2.5.0
 * @author Aliz Rose Frily
 */
public class RegisterNewInterestBtn extends Button {
    @Override
    public String getId() {
        return "new-interest-btn";
    }

    @Override
    public String getLabel() {
        return "Interest registrieren";
    }

    @Override
    public ButtonStyle getStyle() {
        return ButtonStyle.PRIMARY;
    }

    @Override
    public void execute(@NonNull ButtonInteractionEvent event) throws SQLException {
        RegisterNewInterestModal modal = new RegisterNewInterestModal();
        event.replyModal(modal.build()).queue();
    }
}
