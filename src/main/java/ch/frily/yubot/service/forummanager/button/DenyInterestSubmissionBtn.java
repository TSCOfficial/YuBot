package ch.frily.yubot.service.forummanager.button;

import ch.frily.yubot.EnvKey;
import ch.frily.yubot.exception.NotFoundException;
import ch.frily.yubot.interaction.button.Button;
import ch.frily.yubot.service.forummanager.InterestForumManager;
import ch.frily.yubot.service.forummanager.entity.SpecialInterest;
import ch.frily.yubot.service.forummanager.modal.DenySubmissionReasonModal;
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
public class DenyInterestSubmissionBtn extends Button {
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
        String interestId = getArgument(event.getComponentId(), "interestId");
        SpecialInterest interest = SpecialInterestRepository.getInterestById(Integer.parseInt(interestId));

        if (Objects.nonNull(interest)) {
            DenySubmissionReasonModal modal = new DenySubmissionReasonModal();
            modal.setInterest(interest);
            modal.addArgument("interestId", interestId);
            event.replyModal(modal.build()).queue();
        }else {
            throw new NotFoundException("Einsendung '%s' nicht gefunden.".formatted(interestId));
        }
    }

    @Override
    public List<Role> getAllowedRoles() {
        return List.of(
                EnvKey.ROLE_MODERATOR.getAsRole()
        );
    }
}
