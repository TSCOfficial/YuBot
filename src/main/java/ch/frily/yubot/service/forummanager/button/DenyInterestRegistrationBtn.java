package ch.frily.yubot.service.forummanager.button;

import ch.frily.yubot.EnvKey;
import ch.frily.yubot.exception.NotFoundException;
import ch.frily.yubot.interaction.button.Button;
import ch.frily.yubot.service.forummanager.entity.SpecialInterest;
import ch.frily.yubot.service.forummanager.repository.SpecialInterestRepository;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.jspecify.annotations.NonNull;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

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
        event.deferReply(true).queue();
        String interestId = getArgument(event.getComponentId(), "interestId");
        SpecialInterest interest = SpecialInterestRepository.getInterestById(Integer.parseInt(interestId));

        if (Objects.nonNull(interest)) {
            interest.setRequestStatus(SpecialInterest.RequestStatus.DENIED);
            SpecialInterestRepository.updateInterest(interest);

            event.getHook().editOriginal("✅ Interest wurde abgelehnt.").queue();
            event.getMessage().delete().queue();
        }else {
            throw new NotFoundException("Es konnte keine Anfrage gefunden werden.");
        }
    }

    @Override
    public List<Role> getAllowedRoles() {
        return List.of(
                EnvKey.ROLE_MODERATOR.getAsRole()
        );
    }
}
