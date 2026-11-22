package ch.frily.yubot.service.forummanager.button;

import ch.frily.yubot.EnvKey;
import ch.frily.yubot.exception.NotFoundException;
import ch.frily.yubot.interaction.button.Button;
import ch.frily.yubot.service.forummanager.entity.SpecialInterest;
import ch.frily.yubot.service.forummanager.modal.EditAndAcceptInterestModal;
import ch.frily.yubot.service.forummanager.repository.SpecialInterestRepository;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.jspecify.annotations.NonNull;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

public class EditAndAcceptInterestSubmissionBtn extends Button {
    @Override
    public String getId() {
        return "edit-accept-interest-btn";
    }

    @Override
    public String getLabel() {
        return "Bearbeiten & Freigeben";
    }

    @Override
    public ButtonStyle getStyle() {
        return ButtonStyle.SECONDARY;
    }

    @Override
    public List<Role> getAllowedRoles() {
        return List.of(
                EnvKey.ROLE_MODERATOR.getAsRole()
        );
    }

    @Override
    public void execute(@NonNull ButtonInteractionEvent event) throws SQLException {
        String interestId = getArgument(event.getComponentId(), "interestId");
        SpecialInterest interest = SpecialInterestRepository.getInterestById(Integer.parseInt(interestId));

        if (Objects.nonNull(interest)) {
            EditAndAcceptInterestModal modal = new EditAndAcceptInterestModal();
            modal.setInterest(interest);
            modal.addArgument("interestId", interestId);
            event.replyModal(modal.build()).queue();
        } else {
            throw new NotFoundException("Es konnte keine Anfrage gefunden werden.");
        }

    }
}
