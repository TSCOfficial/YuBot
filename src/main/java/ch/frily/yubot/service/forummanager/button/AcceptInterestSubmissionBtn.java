package ch.frily.yubot.service.forummanager.button;

import ch.frily.yubot.EnvKey;
import ch.frily.yubot.exception.NotFoundException;
import ch.frily.yubot.interaction.button.Button;
import ch.frily.yubot.service.forummanager.InterestForumManager;
import ch.frily.yubot.service.forummanager.entity.SpecialInterest;
import ch.frily.yubot.service.forummanager.repository.SpecialInterestRepository;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.jspecify.annotations.NonNull;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

/**
 * Accept an active special-interest registration request
 *
 * @author Aliz Rose Frily
 * @since v2.5.0
 */
@Slf4j
public class AcceptInterestSubmissionBtn extends Button {
    @Override
    public String getId() {
        return "accept-interest-btn";
    }

    @Override
    public String getLabel() {
        return "Freigeben";
    }

    @Override
    public ButtonStyle getStyle() {
        return ButtonStyle.SUCCESS;
    }

    @Override
    public List<Role> getAllowedRoles() {
        return List.of(
                EnvKey.ROLE_MODERATOR.getAsRole()
        );
    }

    @Override
    public void execute(@NonNull ButtonInteractionEvent event) throws SQLException {
        event.deferReply(true).queue();
        String interestId = getArgument(event.getComponentId(), "interestId");
        SpecialInterest interest = SpecialInterestRepository.getInterestById(Integer.parseInt(interestId));

        if (Objects.nonNull(interest)) {
            SpecialInterestRepository.updateInterest(interest);
            InterestForumManager.acceptInterestSubmission(interest);

            event.getHook().editOriginal("✅ Interest wurde freigegeben.").queue();
            event.getMessage().delete().queue();
        } else {
            throw new NotFoundException("Es konnte keine Anfrage gefunden werden.");
        }

    }
}
