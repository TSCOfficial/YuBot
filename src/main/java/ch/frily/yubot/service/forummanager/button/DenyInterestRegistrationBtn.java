package ch.frily.yubot.service.forummanager.button;

import ch.frily.yubot.EnvKey;
import ch.frily.yubot.interaction.button.Button;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.jspecify.annotations.NonNull;

import java.sql.SQLException;
import java.util.List;

/**
 * Deny an active special-interest registration request
 *
 * @author Aliz Rose Frily
 * @since v2.5.0
 */
public class DenyInterestRegistrationBtn extends Button {
    @Override
    public String getId() {
        return "deny-interest-btn";
    }

    @Override
    public String getLabel() {
        return "Ablehnen";
    }

    @Override
    public ButtonStyle getStyle() {
        return ButtonStyle.DANGER;
    }

    @Override
    public void execute(@NonNull ButtonInteractionEvent event) throws SQLException {

    }

    @Override
    public List<Role> getAllowedRoles() {
        return List.of(
                EnvKey.ROLE_MODERATOR.getAsRole()
        );
    }
}
